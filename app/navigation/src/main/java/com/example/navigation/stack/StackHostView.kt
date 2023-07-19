package com.example.navigation.stack

import android.content.Context
import android.util.AttributeSet
import android.util.SparseArray
import androidx.core.animation.addListener
import androidx.core.util.forEach
import androidx.core.util.putAll
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.ObserveLifecycleMode
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.observe
import com.arkivanov.essenty.lifecycle.*
import com.example.navigation.view.AnimationBehaviour
import com.example.navigation.view.HostView
import com.example.navigation.view.ViewRender

class StackHostView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : HostView(context, attrs, defStyleAttr) {

    private var currentStack: ChildStack<*, *>? = null

    fun <C : Any, T : ViewRender> observe(
        stack: Value<ChildStack<C, T>>,
        hostViewLifecycle: Lifecycle, // view lifecycle
        mode: ObserveLifecycleMode = ObserveLifecycleMode.RESUME_PAUSE,
        animationBehaviour: AnimationBehaviour? = null,
    ) {
        this.animationBehaviour = animationBehaviour
        stack.observe(hostViewLifecycle, mode) {
            onStackChanged(it, hostViewLifecycle)
        }
    }

    private fun <C : Any, T : ViewRender> onStackChanged(
        stack: ChildStack<C, T>,
        hostViewLifecycle: Lifecycle,
    ) {
        animator?.end()
        @Suppress("UNCHECKED_CAST")
        val currentStack = currentStack as ChildStack<C, T>?

        @Suppress("UNCHECKED_CAST")
        val currentChild = currentChild as ActiveChild<C, T>?

        if (currentChild?.child?.configuration != stack.active.configuration) {

            val activeChild = createActiveChild(hostViewLifecycle, stack.active)
            this.addView(activeChild.view)

            if (isInBackStack(stack, currentChild)) {
                addActiveToInactive(currentChild)
            }
            validateInactive(stack)
            // Новый экран был в стеке, поэтому проигрываем анимацию в обратную сторону.
            val activeFromStack = isInBackStack(currentStack, activeChild)
            val animation = hasAnimation(currentChild, activeChild, activeFromStack)
            // Нужно анимировать если уже есть view и указана анимация
            if (animation != null && currentChild != null) {
                animateChange(currentChild, activeChild, activeFromStack, animation) {
                    switchCurrent(currentChild, activeChild, stack)
                }
            } else {
                switchCurrent(currentChild, activeChild, stack)
            }
        } else {
            validateInactive(stack)
        }
    }

    private fun switchCurrent(current: ActiveChild<*, *>?, active: ActiveChild<*, *>, stack: ChildStack<*, *>) {
        current?.lifecycle?.destroy()
        active.lifecycle.resume()
        this.removeView(currentChild?.view)
        this.currentChild = active
        this.currentStack = stack
    }

    private fun hasAnimation(
        current: ActiveChild<*, *>?,
        active: ActiveChild<*, *>,
        reverse: Boolean,
    ): AnimationBehaviour?{
        if (current == null) return null
        return if (reverse) {
            current.animationBehaviour
        } else {
            active.animationBehaviour
        }
    }

    private fun animateChange(
        current: ActiveChild<*, *>,
        active: ActiveChild<*, *>,
        reverse: Boolean,
        animation: AnimationBehaviour,
        onEnd: () -> Unit
    ) {
        animator = if (reverse)
            animation.close(current.view, active.view, this)
        else
            animation.open(current.view, active.view, this)

        animator?.addListener(
            onStart = {
                active.lifecycle.start()
                current.lifecycle.pause()
            },
            onEnd = {
                animator = null
                onEnd()
            }
        )
        animator?.start()
    }

    private fun isInBackStack(stack: ChildStack<*, *>?, child: ActiveChild<*, *>?): Boolean{
        return stack != null &&
                child != null &&
                stack.backStack.any { it.configuration == child.child.configuration }
    }

    // Синхронизируем не активные элемнеты с backstack.
    private fun validateInactive(stack: ChildStack<*, *>) {
        val validChild = SparseArray<InactiveChild>()
        val validKeys = stack.backStack.asSequence().map { it.getKey() }.toSet()
        inactiveChildren.forEach { key, child ->
            if (key in validKeys){
                validChild.put(key, child)
            }
        }
        inactiveChildren.clear()
        inactiveChildren.putAll(validChild)
    }
}
