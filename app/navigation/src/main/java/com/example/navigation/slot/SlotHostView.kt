package com.example.navigation.slot

import android.animation.Animator
import android.animation.AnimatorSet
import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.core.animation.addListener
import androidx.core.animation.doOnStart
import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.value.ObserveLifecycleMode
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.observe
import com.arkivanov.essenty.lifecycle.*
import com.example.navigation.view.AnimationBehaviour
import com.example.navigation.view.HostView
import com.example.navigation.view.ViewRender

class SlotHostView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : HostView(context, attrs, defStyleAttr) {

    private var currentSlot: ChildSlot<*, *>? = null
    private var animator: Animator? = null
    private var animationBehaviour: AnimationBehaviour? = null

    fun <C : Any, T : ViewRender> observe(
        slot: Value<ChildSlot<C, T>>,
        hostViewLifecycle: Lifecycle, // view lifecycle
        mode: ObserveLifecycleMode = ObserveLifecycleMode.RESUME_PAUSE,
        animationBehaviour: AnimationBehaviour? = null,
    ) {
        this.animationBehaviour = animationBehaviour
        slot.observe(hostViewLifecycle, mode) {
            onSlotChanged(it, hostViewLifecycle)
        }
    }

    private fun <C : Any, T : ViewRender> onSlotChanged(
        slot: ChildSlot<C, T>,
        hostViewLifecycle: Lifecycle,
    ) {
        animator?.end()
        @Suppress("UNCHECKED_CAST")
        val currentSlot = currentSlot as ChildSlot<C, T>?

        @Suppress("UNCHECKED_CAST")
        val currentChild = currentChild as ActiveChild<C, T>?

        if (currentChild?.child?.configuration != slot.child?.configuration) {
            val activeChild =  if (slot.child != null) {
                val slotChild = slot.child!!
                val created = createActiveChild(hostViewLifecycle,slotChild)
                this.addView(created.view)
                created
            } else {
                null
            }
            clearInactive()
            // Новый экран был в стеке, поэтому проигрываем анимацию в обратную сторону.
            val type = hasAnimation(currentChild, activeChild)
            // Нужно анимировать если уже есть view и указана анимация
            if (type != null) {
                animateChange(currentChild, activeChild, type) {
                    switchCurrent(currentChild, activeChild, slot)
                }
            } else {
                switchCurrent(currentChild, activeChild, slot)
            }
        } else {
            clearInactive()
        }
    }

    private fun switchCurrent(current: ActiveChild<*, *>?, active: ActiveChild<*, *>?, slot: ChildSlot<*, *>) {
        current?.lifecycle?.destroy()
        active?.lifecycle?.resume()
        this.removeView(currentChild?.view)
        this.currentChild = active
        this.currentSlot = slot
    }

    private fun<C : Any, T : ViewRender> hasAnimation(
        current: ActiveChild<C, T>?,
        active: ActiveChild<C, T>?,
    ): ChangeType?{
        return when {
            current == null && active == null -> null
            current != null && active == null -> Close(current, current.child.instance.animationBehaviour ?: animationBehaviour)
            current == null && active != null -> Open(active, active.child.instance.animationBehaviour ?: animationBehaviour)
            current != null && active != null -> Switch(
                current,
                active,
                current.child.instance.animationBehaviour ?: animationBehaviour,
                active.child.instance.animationBehaviour ?: animationBehaviour
            )
            else -> null
        }
    }

    private fun animateChange(
        current: ActiveChild<*, *>?,
        active: ActiveChild<*, *>?,
        changeType: ChangeType,
        onEnd: () -> Unit
    ) {
        animator = changeType.animator(this)
        if (animator != null) {
            animator?.addListener(
                onStart = {
                    active?.lifecycle?.start()
                    current?.lifecycle?.pause()
                },
                onEnd = {
                    animator = null
                    onEnd()
                }
            )
            animator?.start()
        } else {
            onEnd()
        }
    }

    // Синхронизируем не активные элемнеты с backstack.
    private fun clearInactive() {
        inactiveChildren.clear()
    }

    private sealed interface ChangeType {
        fun animator(parent: View): Animator?
    }

    private class Open(
        val child: ActiveChild<Any, Any>,
        val animationBehaviour: AnimationBehaviour?
    ): ChangeType {

        override fun animator(parent: View): Animator? {
            return animationBehaviour?.open(parent, child.view)
        }
    }

    private class Close(
        val child: ActiveChild<Any, Any>,
        val animationBehaviour: AnimationBehaviour?
    ): ChangeType {

        override fun animator(parent: View): Animator? {
            return animationBehaviour?.close(child.view, parent)
        }
    }

    private class Switch(
        val closeChild: ActiveChild<Any, Any>,
        val openChild: ActiveChild<Any, Any>,
        val closeBehaviour: AnimationBehaviour?,
        val openBehaviour: AnimationBehaviour?
    ): ChangeType {
        override fun animator(parent: View): Animator? {
            val closeAnimator = closeBehaviour?.close(closeChild.view, parent)
            val openAnimator = openBehaviour?.open(parent, openChild.view)
            if  (openAnimator != null) {
                openChild.view.visibility = GONE
                openAnimator.doOnStart { openChild.view.visibility = VISIBLE }
            }
            return when {
                closeAnimator != null && openAnimator != null -> AnimatorSet().apply { playSequentially(closeAnimator, openAnimator) }
                closeAnimator != null && openAnimator == null -> closeAnimator
                closeAnimator == null && openAnimator != null -> openAnimator
                else -> null
            }
        }

    }

}
