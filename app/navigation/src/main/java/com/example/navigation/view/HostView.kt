package com.example.navigation.view

import android.content.Context
import android.os.Parcelable
import android.util.AttributeSet
import android.util.SparseArray
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.doOnPreDraw
import androidx.transition.Transition
import androidx.transition.TransitionManager
import com.arkivanov.decompose.Child
import com.arkivanov.decompose.InternalDecomposeApi
import com.arkivanov.decompose.lifecycle.MergedLifecycle
import com.arkivanov.essenty.lifecycle.*
import kotlinx.parcelize.Parcelize

open class HostView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : CorrectFrameLayout(context, attrs, defStyleAttr) {

    protected var currentChild: ActiveChild<*, *>? = null
    private val inactiveChildren = HashMap<Parcelable, InactiveChild>()
    protected var transitionProvider: TransitionProvider? = null

    /**
     * Класс который описывает текущий открытый экран.
     */
    protected inner class ActiveChild<out C : Any, out T : Any>(
        internal val child: Child.Created<C, T>,
        internal val lifecycle: LifecycleRegistry,
        internal val view: View,
    ) {
        internal val id: Parcelable = child.id()
        val transition: Transition?
        get() =
            (child.configuration as? TransitionProvider)?.transition ?:
            (child.instance as? TransitionProvider)?.transition ?:
            this@HostView.transitionProvider?.transition
    }

    /**
     * Класс который хранит состояния предыдущих экранов.
     * На случай если захотим к ним вернуться.
     */
    @Parcelize
    protected class InactiveChild(
        internal val savedState: SparseArray<Parcelable>,
    ) : Parcelable

    @Parcelize
    private class SavedState(
        val hostState: Parcelable?,
        val childStates: HashMap<Parcelable, InactiveChild>,
    ) : Parcelable

    protected fun endTransition() {
        TransitionManager.endTransitions(this)
    }

    protected fun beginTransition(
        transition: Transition?,
        onStart: () -> Unit,
        onEnd: () -> Unit,
        changes: () -> Unit,
    ) {
        // Корректная последовательность: Применяем изменения -> Старт анимации -> Конец анимации
        if (transition == null) {
            changes.invoke()
            onStart()
            onEnd()
            return
        }
        transition.addCallbacks(onStart = { onStart() }, onEnd = { onEnd() })
        // beginDelayedTransition работает только если sceneRoot.isLaidOut() == true
        if (ViewCompat.isLaidOut(this)) {
            TransitionManager.beginDelayedTransition(this, transition)
            changes.invoke()
        } else {
            // doOnLayout вызывается до присваивания isLaidOut = true, поэтому не работает
            doOnPreDraw {
                TransitionManager.beginDelayedTransition(this, transition)
                changes.invoke()
            }
        }
    }

    /**
     * Удаляет из inactiveChildren состояния, которые не находятся в valid.
     * Нужно чтобы не держать в памяти лишние сохраненные состояния View.
     */
    protected fun validateInactive(valid: Collection<Child<*, *>>?){
        val validId = valid?.asSequence()?.map { it.id() }?.toSet() ?: emptySet<Child<*, *>>()
        val validChildren = HashMap<Parcelable, InactiveChild>()
        inactiveChildren.forEach { (id, inactive) ->
            if (id in validId){
                validChildren[id] = inactive
            }
        }
        inactiveChildren.clear()
        inactiveChildren.putAll(validChildren)
    }

    /**
     * Создает новый активный экран.
     * Вызывает создание View. Привязывает ЖЦ к родительскому ЖЦ.
     * ЖЦ view = CREATED
     */
    @OptIn(InternalDecomposeApi::class)
    protected fun <C : Any, T : ViewRender> createActiveChild(
        hostViewLifecycle: Lifecycle,
        child: Child.Created<C, T>
    ): ActiveChild<C, T> {
        val lifecycle = LifecycleRegistry()
        val view = child.instance.createView(this,  MergedLifecycle(hostViewLifecycle, lifecycle))
        val activeChild = ActiveChild(child, lifecycle, view)
        lifecycle.doOnStart {
            restoreActive(activeChild)
        }
        lifecycle.create()
        return activeChild
    }

    /**
     * Восстанавливает состояние ActiveChild.
     * Если оно находится в inactiveChildren.
     */
    protected fun restoreActive(active: ActiveChild<*, *>){
        val inactiveChild: InactiveChild? = inactiveChildren[active.id]
        if (inactiveChild != null) {
            active.view.restoreHierarchyState(inactiveChild.savedState)
        }
    }

    /**
     * Сохраняет состояние ActiveChild.
     * Добавляет в inactiveChildren.
     */
    protected fun addActiveToInactive(
        active: ActiveChild<*, *>?,
    ) {
        if (active == null) return
        inactiveChildren[active.id] = InactiveChild(active.view.saveHierarchyState())
    }

    /**
     * Переопределяем дефолтную реализацию сохранения состояния.
     * Обычно сохраняются только состояние ViewGroup и всех вложенных View.
     * HostView содержит только одну активную View, но также нужно сохранить состояния View,
     * которые были прикреплены ранее и к которым пользователь может вернуться.
     */
    override fun dispatchSaveInstanceState(container: SparseArray<Parcelable>) {
        if (id == NO_ID) return
        addActiveToInactive(currentChild)
        container.put(id, onSaveInstanceState())
    }

    /**
     * Переопределяем дефолтную реализацию восстановления состояния.
     * Нужно восстановить состояние активной View не из container: SparseArray<Parcelable>,
     * а из inactiveChildren.
     */
    override fun dispatchRestoreInstanceState(container: SparseArray<Parcelable>) {
        if (id == NO_ID) return
        onRestoreInstanceState(container[id])
        val active = currentChild ?: return
        restoreActive(active)
    }

    override fun onSaveInstanceState(): Parcelable? {
        return SavedState(
            hostState = super.onSaveInstanceState(),
            childStates = inactiveChildren,
        )
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        val savedState = state as SavedState? ?: return
        super.onRestoreInstanceState(savedState.hostState)
        inactiveChildren.clear()
        inactiveChildren.putAll(savedState.childStates)
    }

    protected companion object {

        fun View.saveHierarchyState(): SparseArray<Parcelable> =
            SparseArray<Parcelable>()
                .also(::saveHierarchyState)

        /**
         * Сопоставляет Child некоторый Parcelable, который является уникальным ключем.
         * Под этим ключем сохраняется состояние View для Child, поэтому Parcelable.
         *
         * Нельзя использовать configuration.hashCode, поскольку дефолтная реализация
         * возвращает ссылку на RAM память, следовательно может меняться при перезапуске процесса.
         * А один и тотже объект может генерировать разный hashCode,
         * который не будет равен сохраненному в память на диске.
         *
         * Нельзя использовать View.Id поскольку он не известен для Child.
         *
         * В случае если configuration не является Parcelable, используем hash и string.
         * Надеемся что они переопределены и устойчивы к перезапуску процесса.
         * С data class и data object проблем быть не должно.
         * С class и object состояние может не восстанавиться.
         */
        fun <C : Any, T : Any >Child<C, T>.id(): Parcelable {
            return configuration as? Parcelable ?: AnyParcelable(configuration)
        }
    }

    @Parcelize
    private data class AnyParcelable(
        val hash: Int,
        val str: String,
    ): Parcelable {
        constructor(any: Any): this(any.hashCode(), any.toString())
    }
}
