package com.example.navigation.view

import android.animation.Animator
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.util.SparseArray
import android.view.View
import android.widget.FrameLayout
import androidx.core.animation.addListener
import androidx.core.util.forEach
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

    protected class ActiveChild<out C : Any, out T : Any>(
        val key: Int,
        val child: Child.Created<C, T>,
        val lifecycle: LifecycleRegistry,
        val view: View,
    )

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
        val bundle = Bundle()
        addActiveToInactive(currentChild)
        bundle.putSparseParcelableArray(BUNDLE_INACTIVE_KEY, inactiveChildren)
        return SavedState(superState = super.onSaveInstanceState(), state = bundle)
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        val savedState = state as SavedState? ?: return
        super.onRestoreInstanceState(savedState.superState)
        val restored = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            savedState.state.getSparseParcelableArray(BUNDLE_INACTIVE_KEY, InactiveChild::class.java)
        } else {
            savedState.state.getSparseParcelableArray<InactiveChild>(BUNDLE_INACTIVE_KEY)
        }
        restored?.let {
            inactiveChildren.putAll(it)
        }
    }

    @Parcelize
    private class SavedState(
        val superState: Parcelable?,
        val state: Bundle
    ) : Parcelable

    private companion object {

        const val BUNDLE_INACTIVE_KEY = "BUNDLE_INACTIVE_KEY"

        fun View.saveHierarchyState(): SparseArray<Parcelable> =
            SparseArray<Parcelable>()
                .also(::saveHierarchyState)
    }

}
