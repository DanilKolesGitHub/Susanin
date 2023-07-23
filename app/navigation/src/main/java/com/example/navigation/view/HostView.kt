package com.example.navigation.view

import android.content.Context
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
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
        val child: Child.Created<C, T>,
        val lifecycle: LifecycleRegistry,
        val view: View,
    ) {
        internal val parcelable: Parcelable = child.toParcelable()
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
        val savedState: SparseArray<Parcelable>,
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
        if (transition == null) {
            changes.invoke()
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

    protected fun validateInactive(valid: Collection<Child<*, *>>){
        Log.d("SAVEDEB", "pre validate ${inactiveChildren.size}")
        val validConfigurations = valid.asSequence().map { it.toParcelable() }.toSet()
        val validChildren = HashMap<Parcelable, InactiveChild>()
        inactiveChildren.forEach { (configuration, inactive) ->
            if (configuration in validConfigurations){
                validChildren[configuration] = inactive
            }
        }
        Log.d("SAVEDEB", "post validate ${validChildren.size}")
        inactiveChildren.clear()
        inactiveChildren.putAll(validChildren)
    }

    @OptIn(InternalDecomposeApi::class)
    // Создаем новый активный child.
    protected fun <C : Any, T : ViewRender> createActiveChild(
        hostViewLifecycle: Lifecycle,
        child: Child.Created<C, T>
    ): ActiveChild<C, T> {
        val lifecycle = LifecycleRegistry()
        val activeChildView = child.instance.createView(this,  MergedLifecycle(hostViewLifecycle, lifecycle))
        lifecycle.create()
        // Если было сохранено состояние, то восстанавливаем его.
        Log.d("SAVEDEB", "createActive ${activeChildView.ids()}")
        val activeChild = ActiveChild(
            child,
            lifecycle,
            activeChildView
        )
        lifecycle.doOnStart {
            restoreActive(activeChild)
        }
        return activeChild
    }

    private fun restoreActive(active: ActiveChild<*, *>){
        Log.d("SAVEDEB", "restoreActive ${active.view.ids()}")
        inactiveChildren.forEach { (id, child) ->
            Log.d("SAVEDEB", "inactive ${id}")
        }
        val inactiveChild: InactiveChild? = inactiveChildren[active.parcelable]
        if (inactiveChild != null) {
            active.view.restoreHierarchyState(inactiveChild.savedState)
        }
    }

    protected fun addActiveToInactive(
        active: ActiveChild<*, *>?,
    ) {
        //  Сохраняем состояние текущего экрана.
        if (active == null) return
        val inactive = InactiveChild(
            active.view.saveHierarchyState()
        )
        inactiveChildren[active.parcelable] = inactive
    }

    private fun <C : Any, T : Any >Child<C, T>.toParcelable(): Parcelable {
        return configuration as? Parcelable ?: AnyParcelable(configuration)
    }

    override fun dispatchSaveInstanceState(container: SparseArray<Parcelable>) {
        Log.d("SAVEDEB", "dispatchSaveInstanceState ${this.ids()}")
        if (id == NO_ID) return
        addActiveToInactive(currentChild)
        container.put(id, onSaveInstanceState())
    }

    override fun dispatchRestoreInstanceState(container: SparseArray<Parcelable>) {
        Log.d("SAVEDEB", "dispatchRestoreInstanceState ${this.ids()}")
        if (id == NO_ID) return
        onRestoreInstanceState(container[id])
        val active = currentChild ?: return
        restoreActive(active)
    }

    override fun onSaveInstanceState(): Parcelable? {
        Log.d("SAVEDEB", "onSaveInstanceState ${this.ids()}")
        return SavedState(
            hostState = super.onSaveInstanceState(),
            childStates = inactiveChildren,
        )
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        Log.d("SAVEDEB", "onRestoreInstanceState ${this.ids()}")
        val savedState = state as SavedState? ?: return
        super.onRestoreInstanceState(savedState.hostState)
        inactiveChildren.clear()
        inactiveChildren.putAll(savedState.childStates)
    }

    fun Int.ids(): String {
        return if (this == NO_ID) "no-id" else resources.getResourceName(this)
    }

    fun View.ids(): String {
        return if (id == NO_ID) "no-id" else this.resources.getResourceName(id)
    }

    private companion object {

        fun View.saveHierarchyState(): SparseArray<Parcelable> =
            SparseArray<Parcelable>()
                .also(::saveHierarchyState)
    }

    @Parcelize
    private data class AnyParcelable(
        val hash: Int,
        val str: String,
    ): Parcelable {
        constructor(any: Any): this(any.hashCode(), any.toString())
    }

}
