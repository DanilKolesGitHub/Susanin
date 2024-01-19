package com.example.navigation.view

import android.animation.Animator
import android.content.Context
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.util.SparseArray
import android.view.View
import android.widget.FrameLayout
import androidx.core.animation.addListener
import androidx.core.view.ViewCompat
import androidx.core.view.doOnPreDraw
import com.arkivanov.decompose.Child
import com.arkivanov.decompose.InternalDecomposeApi
import com.arkivanov.decompose.lifecycle.MergedLifecycle
import com.arkivanov.essenty.lifecycle.*
import kotlinx.parcelize.Parcelize

abstract class HostView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    // Состояния верстки view в backstack.
    private val inactiveChildren = HashMap<Parcelable, InactiveChild>()
    // Проигрываемая в данный момент анимация.
    private var animator: Animator? = null
    // Параметры переданные в HostView.
    protected var uiParams: UiParams? = null

    /**
     * Класс который описывает текущий открытый экран.
     */
    protected inner class ActiveChild<out C : Any, out T : Any>(
        internal val child: Child.Created<C, T>,
        internal val lifecycle: LifecycleRegistry,
        internal val view: View,
    ) {
        internal val id: Parcelable = child.id()
        internal val overlay: Boolean get() = child.overlay
        internal val viewTransition: ViewTransition? get() = child.viewTransition
    }

    protected val Child.Created<*, *>.overlay: Boolean get() =
            (configuration as? UiParams)?.overlay ?:
            (instance as? UiParams)?.overlay ?:
            this@HostView.uiParams?.overlay ?: false


    protected val Child.Created<*, *>.viewTransition: ViewTransition? get() =
        (configuration as? UiParams)?.viewTransition ?:
        (instance as? UiParams)?.viewTransition ?:
        this@HostView.uiParams?.viewTransition

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

    /**
     * Отмена текущей анимации.
     */
    protected fun endTransition() {
        animator?.end()
        animator = null
    }

    protected fun beginTransition(
        addToBack: Boolean,
        add: ActiveChild<*, *>?,
        remove: ActiveChild<*, *>?,
        animatorProvider: () -> Animator?,
        onStart: () -> Unit,
        onEnd: () -> Unit,
    ) {
        beginTransition(
            addToBack = addToBack,
            addChildren = listOfNotNull(add),
            removeChildren = listOfNotNull(remove),
            animatorProvider = animatorProvider,
            onStart = onStart,
            onEnd = onEnd,
        )
    }

    /**
     * Запускает анимацию изменения иерархии.
     * Если нет анимации, то изменения происходят последовательно без анимации.
     *
     * Метод принимает addChildren, которые нужно добавить,
     * и removeChildren, которые нужно удалить.
     *
     * Перед началом анимации вызывается add, который добавляет новые view в иерархию.
     * После чего метод дожидается завершения onMeasure и onLayout (см doOnReady).
     * Это нужно чтобы перед получением анимации у view уже были правильные размеры.
     * После анимации вызывается remove, который удаляет проанимированные view.
     *
     * @param addToBack true - добавить новые view под текущие, false - добавить поверх.
     * @param addChildren View, которые нужно добавить.
     * @param removeChildren View, которые нужно удалить.
     * @param animatorProvider Предоставляет анимацию. Вызывается после добавления новых view.
     * @param onStart Вызывается перед анимацией.
     * @param onEnd Вызывается после анимации.
     */
    protected fun beginTransition(
        addToBack: Boolean,
        addChildren: Collection<ActiveChild<*, *>>,
        removeChildren: Collection<ActiveChild<*, *>>,
        animatorProvider: () -> Animator?,
        onStart: () -> Unit,
        onEnd: () -> Unit,
    ) = doOnReady {// Если на момент вызова HostView еще не isLaidOut, дожидаемся окончания.
        add(
            back = addToBack,
            children = addChildren,
        ) {
            transition(
                animatorProvider = animatorProvider,
                onStart = onStart,
                onEnd = onEnd,
            ) {
                remove(
                    children = removeChildren
                )
            }
        }
    }

    /**
     *
     */
    private inline fun View.doOnReady(crossinline action: (view: View) -> Unit) {
        if (ViewCompat.isLaidOut(this) && !isLayoutRequested) {
            action(this)
        } else {
            doOnPreDraw {
                action(it)
            }
        }
    }

    /**
     * Добавляет view в текущую иерархию HostView.
     * Если добавляемая view уже есть в иерархии, то она удалится и добавится заново.
     * @param children view которые нужно добавить.
     * @param back Если true то новые view добавляются в начало иерархии, иначе в конец.
     * @param onAdd Колбэк после добавления в иерархию.
     */
    private inline fun add(
        back: Boolean,
        children: Collection<ActiveChild<*, *>>,
        crossinline onAdd: () -> Unit,
    ) {
        if (children.isEmpty()) {
            onAdd()
            return
        }
        // чтобы не вызывать requestLayout после каждого изменения используются методы ...InLayout.
        // если view уже добавлены в host, то удаляем их, чтобы расположить в правильном порядке.
        children.forEach { child ->
            removeViewInLayout(child.view)
        }
        children.forEachIndexed { index, child ->
            if (back) { // если это возвращение назад, то вставляем view под текущие.
                addViewInLayout(child.view, index, child.view.layoutParams)
            } else { // если это добавление новых, то вставляем поверх текущих.
                addViewInLayout(child.view, -1, child.view.layoutParams)
            }
        }
        requestLayout() // После добавления запрашиваем requestLayout
        log("add")
        doOnReady {// Дожидаемся пока выполнится layout после добавления
            // Тут новые view добавлены и измерены. Можно запускать анимацию.
            onAdd()
        }
    }

    /**
     * Анимирует изменение иерархии.
     */
    private inline fun transition(
        animatorProvider: () -> Animator?,
        crossinline onStart: () -> Unit,
        crossinline onEnd: () -> Unit,
        crossinline onAnimated: () -> Unit,
    ) {
        animator = animatorProvider() // Получаем анимацию
        if (animator == null) { //
            onStart()
            onEnd()
            onAnimated()
        } else {
            animator!!.addListener(
                onStart = {
                    onStart()
                },
                onEnd = {
                    animator = null
                    onEnd()
                    onAnimated()
                },
            )
            animator!!.start()
            log("animate")
        }
    }

    /**
     * Удаляет view из текущей иерархии HostView.
     * @param children view которые нужно удалить.
     */
    protected fun remove(
        children: Collection<ActiveChild<*, *>>
    ) {
        if (children.isEmpty()) return
        // чтобы не вызывать requestLayout после каждого изменения используются методы ...InLayout.
        // удалем view.
        children.forEach { child ->
            removeViewInLayout(child.view)
        }
        requestLayout() // После удаления запрашиваем requestLayout
    }

    private fun log(text: String){
        Log.d("VDEB", "$text ${resources.getResourceName(id)}")
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
    protected fun restoreActive(active: ActiveChild<*, *>?){
        active ?: return
        val inactiveChild: InactiveChild? = inactiveChildren[active.id]
        if (inactiveChild != null) {
            active.view.restoreHierarchyState(inactiveChild.savedState)
        }
    }

    /**
     * Сохраняет состояние ActiveChild.
     * Добавляет в inactiveChildren.
     */
    protected fun saveActive(
        active: ActiveChild<*, *>?,
    ) {
        active ?: return
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
        saveActive()
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
        restoreActive()
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

    /**
     * Вызывается в момент сохранения состояния HostView.
     * Внутри нужно сохранить все активные View.
     * Используй метод saveActive(ActiveChild)
     * @see saveActive(ActiveChild)
     */
    protected abstract fun saveActive()

    /**
     * Вызывается в момент восстановления состояния HostView.
     * Внутри нужно восстановить все активные View.
     * Используй метод restoreActive(ActiveChild)
     * @see restoreActive(ActiveChild)
     */
    protected abstract fun restoreActive()

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
