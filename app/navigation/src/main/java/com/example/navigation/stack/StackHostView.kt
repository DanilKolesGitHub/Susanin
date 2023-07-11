package com.example.navigation.stack

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
import androidx.core.view.forEach
import com.arkivanov.decompose.Child
import com.arkivanov.decompose.InternalDecomposeApi
import com.arkivanov.decompose.lifecycle.MergedLifecycle
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.ObserveLifecycleMode
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.observe
import com.arkivanov.essenty.lifecycle.*
import com.example.navigation.R
import com.example.navigation.view.AnimationBehaviour
import com.example.navigation.view.ViewRender
import kotlinx.android.parcel.Parcelize

class StackHostView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var currentStack: ChildStack<*, *>? = null
    private var currentChild: ActiveChild<*, *>? = null
    private val inactiveChildren = SparseArray<InactiveChild>()

    fun <C : Any, T : ViewRender> observe(
        stack: Value<ChildStack<C, T>>,
        hostViewLifecycle: Lifecycle, // view lifecycle
        mode: ObserveLifecycleMode = ObserveLifecycleMode.RESUME_PAUSE,
        animationBehaviour: AnimationBehaviour? = null,
    ) {
        stack.observe(hostViewLifecycle, mode) {
            onStackChanged(it, hostViewLifecycle, animationBehaviour)
        }
    }

    private class ActiveChild<out C : Any, out T : Any>(
        val key: Int,
        val child: Child.Created<C, T>,
        val lifecycle: LifecycleRegistry,
    )

    @Parcelize
    private class InactiveChild(
        val key: Int,
        val savedState: SparseArray<Parcelable>,
    ): Parcelable

    private fun findChildView(key: Int): View? {
        forEach {
            if (it.key == key) {
                return it
            }
        }
        return null
    }

    private var View.key: Int?
        get() = getTag(R.id.stack_view_marker_key) as Int?
        set(value) {
            setTag(R.id.stack_view_marker_key, value)
        }

    private fun <C : Any, T : ViewRender> onStackChanged(
        stack: ChildStack<C, T>,
        hostViewLifecycle: Lifecycle,
        animationBehaviour: AnimationBehaviour? = null,
    ) {

        @Suppress("UNCHECKED_CAST")
        val currentStack = currentStack as ChildStack<C, T>?

        @Suppress("UNCHECKED_CAST")
        val currentChild = currentChild as ActiveChild<C, T>?

        if (currentChild?.child?.configuration != stack.active.configuration) {
            val currentChildView = currentChild?.let { findChildView(it.key) }
            addActiveToInactive(stack, currentChild, currentChildView)
            val activeChild = createActiveChild(stack.active)
            val activeChildView = createActiveChildView(activeChild, hostViewLifecycle)
            validateInactive(stack)
            // Новый экран был в стеке, поэтому проигрываем анимацию в обратную сторону.
            val activeFromStack = currentStack
                ?.backStack
                ?.any { it.configuration == activeChild.child.configuration }
                ?: false
            val animation = if (activeFromStack) {
                currentChild?.child?.instance?.animationBehaviour
            } else {
                activeChild.child.instance.animationBehaviour
            } ?: animationBehaviour
            // Нужно анимировать если уже есть view и указана анимация
            if (currentChildView != null && currentStack != null && animation != null) {
                val animator: Animator = if (activeFromStack)
                    animation.close(currentChildView, activeChildView)
                else
                    animation.open(currentChildView, activeChildView)
                this.addView(activeChildView)
                animator.addListener(
                    onStart = {
                        activeChild.lifecycle.start()
                        currentChild.lifecycle.pause()
                    },
                    onEnd = {
                        activeChild.lifecycle.resume()
                        currentChild.lifecycle.destroy()
                        this.removeView(currentChildView)
                        this.currentChild = activeChild
                        this.currentStack = stack
                    }
                )
                animator.start()
            } else {
                this.addView(activeChildView)
                activeChild.lifecycle.resume()
                currentChild?.lifecycle?.destroy()
                this.removeView(currentChildView)
                this.currentChild = activeChild
                this.currentStack = stack
            }
        } else {
            validateInactive(stack)
        }
    }

    // Добавляем предыдущий активный элемент в неактивные.
    private fun addActiveToInactive(
        stack: ChildStack<*, *>,
        child: ActiveChild<*, *>?,
        childView: View?,
    ) {
        if (childView == null || child == null) return
        if (stack.backStack.any { it.configuration == child.child.configuration }) {
            inactiveChildren[child.key] = InactiveChild(
                child.key,
                childView.saveHierarchyState()
            )
        }
    }

    // Синхронизируем не активные элемнеты с backstack.
    private fun validateInactive(stack: ChildStack<*, *>) {
        val validChild = SparseArray<InactiveChild>()
        val validKeys = stack.backStack.asSequence().map { it.configuration.getKey() }.toSet()
        inactiveChildren.forEach { key, child ->
            if (key in validKeys){
                validChild.put(key, child)
            }
        }
        inactiveChildren.clear()
        inactiveChildren.putAll(validChild)
    }

    // Создаем новый активный child.
    private fun <C : Any, T : Any> createActiveChild(child: Child.Created<C, T>): ActiveChild<C, T> {
        return ActiveChild(
            child.configuration.getKey(),
            child,
            LifecycleRegistry()
        )
    }

    // Создаем новый активный child view и восстанавливаем состояние.
    @OptIn(InternalDecomposeApi::class)
    private fun <C : Any, T : ViewRender> createActiveChildView(child: ActiveChild<C, T>, hostViewLifecycle: Lifecycle): View {
        val activeChildView = child.child.instance.createView(this,  MergedLifecycle(hostViewLifecycle, child.lifecycle))
        child.lifecycle.create()
        val inactiveChild: InactiveChild? = inactiveChildren[child.key]
        if (inactiveChild != null) {
            activeChildView.restoreHierarchyState(inactiveChild.savedState)
        }
        return activeChildView
    }

    override fun onSaveInstanceState(): Parcelable {
        val bundle = Bundle()
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

        fun <C : Any> C.getKey(): Int {
            return hashCode()
        }
    }

}
