package com.example.navigation.slot

import android.animation.Animator
import android.animation.AnimatorSet
import android.content.Context
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
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
    private var afterRestore: Boolean = false

    fun <C : Any, T : ViewRender> observe(
        slot: Value<ChildSlot<C, T>>,
        hostViewLifecycle: Lifecycle, // view lifecycle
        animationBehaviour: AnimationBehaviour? = null,
    ) {
        this.animationBehaviour = animationBehaviour
        hostViewLifecycle.doOnDestroy { animator?.end() }
        slot.observe(hostViewLifecycle) {
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
            if (type != null && !afterRestore) {
                animateChange(currentChild, activeChild, type) {
                    switchCurrent(currentChild, activeChild, slot)
                }
            } else {
                switchCurrent(currentChild, activeChild, slot)
            }
        } else {
            clearInactive()
        }
        afterRestore = false
    }

    private fun switchCurrent(current: ActiveChild<*, *>?, active: ActiveChild<*, *>?, slot: ChildSlot<*, *>) {
        current?.lifecycle?.destroy()
        active?.lifecycle?.resume()
        this.removeView(currentChild?.view)
        this.currentChild = active
        this.currentSlot = slot
    }

    private fun hasAnimation(
        current: ActiveChild<*, *>?,
        active: ActiveChild<*, *>?,
    ): ChangeType?{
        return when {
            current != null && active == null -> Close(current)
            current == null && active != null -> Open(active)
            current != null && active != null -> Switch(current, active)
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
        fun animator(parent: ViewGroup): Animator?
    }

    private class Open(
        val child: ActiveChild<*, *>,
    ): ChangeType {

        override fun animator(parent: ViewGroup): Animator? {
            return child.animationBehaviour?.open(parent, child.view, parent)
        }
    }

    private class Close(
        val child: ActiveChild<*, *>,
    ): ChangeType {

        override fun animator(parent: ViewGroup): Animator? {
            child.view.bringToFront()
            return child.animationBehaviour?.close(child.view, parent, parent)
        }
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        afterRestore = state != null
        super.onRestoreInstanceState(state)
    }

    private class Switch(
        val closeChild: ActiveChild<*, *>,
        val openChild: ActiveChild<*, *>,
    ): ChangeType {

        override fun animator(parent: ViewGroup): Animator? {
            closeChild.view.bringToFront()
            val closeAnimator = closeChild.animationBehaviour?.close(closeChild.view, parent, parent)
            val openAnimator = openChild.animationBehaviour?.open(parent, openChild.view, parent)
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
