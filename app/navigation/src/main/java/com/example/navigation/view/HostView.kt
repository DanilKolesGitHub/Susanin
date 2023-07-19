package com.example.navigation.view

import android.animation.Animator
import android.content.Context
import android.os.Parcelable
import android.util.AttributeSet
import android.util.SparseArray
import android.view.View
import android.widget.FrameLayout
import androidx.core.util.putAll
import com.arkivanov.decompose.Child
import com.arkivanov.decompose.InternalDecomposeApi
import com.arkivanov.decompose.lifecycle.MergedLifecycle
import com.arkivanov.essenty.lifecycle.*
import kotlinx.android.parcel.Parcelize

open class HostView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    protected var currentChild: ActiveChild<*, *>? = null
    protected val inactiveChildren = SparseArray<InactiveChild>()
    protected var animationBehaviour: AnimationBehaviour? = null
    protected var animator: Animator? = null

    protected inner class ActiveChild<out C : Any, out T : Any>(
        val key: Int,
        val child: Child.Created<C, T>,
        val lifecycle: LifecycleRegistry,
        val view: View,
    ) {

        val animationBehaviour: AnimationBehaviour?
        get() {
            return (child.configuration as? AnimationProvider)?.animationBehaviour ?:
            (child.instance as? AnimationProvider)?.animationBehaviour ?:
            this@HostView.animationBehaviour
        }
    }

    @Parcelize
    protected class InactiveChild(
        val key: Int,
        val savedState: SparseArray<Parcelable>,
    ): Parcelable

    protected fun addActiveToInactive(
        child: ActiveChild<*, *>?,
    ) {
        if (child?.view == null) return
        inactiveChildren[child.key] = InactiveChild(
            child.key,
            child.view.saveHierarchyState()
        )
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
            animationBehaviour = animationBehaviour
        )
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        val savedState = state as SavedState? ?: return
        super.onRestoreInstanceState(savedState.superState)
        inactiveChildren.clear()
        inactiveChildren.putAll(savedState.inactiveChildren)
        animationBehaviour = savedState.animationBehaviour
    }

    @Parcelize
    private class SavedState(
        val superState: Parcelable?,
        val inactiveChildren: SparseArray<InactiveChild>,
        val animationBehaviour: AnimationBehaviour?,
    ) : Parcelable

    private companion object {

        fun View.saveHierarchyState(): SparseArray<Parcelable> =
            SparseArray<Parcelable>()
                .also(::saveHierarchyState)
    }

}
