package com.example.navigation.view

import android.content.Context
import android.os.Parcelable
import android.util.AttributeSet
import android.util.SparseArray
import android.view.View
import androidx.core.util.putAll
import androidx.transition.Transition
import androidx.transition.TransitionManager
import com.arkivanov.decompose.Child
import com.arkivanov.decompose.InternalDecomposeApi
import com.arkivanov.decompose.lifecycle.MergedLifecycle
import com.arkivanov.essenty.lifecycle.*
import kotlinx.android.parcel.Parcelize

open class HostView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : CorrectFrameLayout(context, attrs, defStyleAttr) {

    protected var currentChild: ActiveChild<*, *>? = null
    protected val inactiveChildren = SparseArray<InactiveChild>()
    protected var transitionProvider: TransitionProvider? = null

    /**
     * Класс который описывает текущий открытый экран.
     */
    protected inner class ActiveChild<out C : Any, out T : Any>(
        val key: Int,
        val child: Child.Created<C, T>,
        val lifecycle: LifecycleRegistry,
        val view: View,
    ) {
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
        val key: Int,
        val savedState: SparseArray<Parcelable>,
    ): Parcelable

    protected fun addActiveToInactive(
        child: ActiveChild<*, *>?,
    ) {
        //  Сохраняем состояние текущего экрана.
        if (child?.view == null) return
        inactiveChildren[child.key] = InactiveChild(
            child.key,
            child.view.saveHierarchyState()
        )
    }

    protected fun endTransition() {
        TransitionManager.endTransitions(this)
    }

    protected fun beginTransition(
        transition: Transition?,
        onStart: () -> Unit,
        onEnd: () -> Unit,
    ) {
        if (transition == null) {
            onEnd()
            return
        }
        transition.addCallbacks(onStart = { onStart() }, onEnd = { onEnd() })
        TransitionManager.beginDelayedTransition(this, transition)
    }

    @OptIn(InternalDecomposeApi::class)
    // Создаем новый активный child.
    protected fun <C : Any, T : ViewRender> createActiveChild(
        hostViewLifecycle: Lifecycle,
        child: Child.Created<C, T>
    ): ActiveChild<C, T> {
        val lifecycle = LifecycleRegistry()
        val key = child.getKey()
        val activeChildView = child.instance.createView(this,  MergedLifecycle(hostViewLifecycle, lifecycle))
        lifecycle.create()
        // Если было сохранено состояние, то восстанавливаем его.
        val inactiveChild: InactiveChild? = inactiveChildren[key]
        if (inactiveChild != null) {
            activeChildView.restoreHierarchyState(inactiveChild.savedState)
        }
        return ActiveChild(
            key,
            child,
            lifecycle,
            activeChildView
        )
    }

    protected fun <C : Any, T : Any >Child<C, T>.getKey(): Int {
        return configuration.hashCode()
    }

    override fun onSaveInstanceState(): Parcelable {
        addActiveToInactive(currentChild)
        return SavedState(
            superState = super.onSaveInstanceState(),
            inactiveChildren = inactiveChildren,
            transitionProvider = transitionProvider
        )
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        val savedState = state as SavedState? ?: return
        super.onRestoreInstanceState(savedState.superState)
        inactiveChildren.clear()
        inactiveChildren.putAll(savedState.inactiveChildren)
        transitionProvider = savedState.transitionProvider
    }

    @Parcelize
    private class SavedState(
        val superState: Parcelable?,
        val inactiveChildren: SparseArray<InactiveChild>,
        val transitionProvider: TransitionProvider?,
    ) : Parcelable

    private companion object {

        fun View.saveHierarchyState(): SparseArray<Parcelable> =
            SparseArray<Parcelable>()
                .also(::saveHierarchyState)
    }

}
