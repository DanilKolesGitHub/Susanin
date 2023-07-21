package com.example.navigation.pages

import android.content.Context
import android.util.AttributeSet
import android.util.SparseArray
import androidx.core.animation.addListener
import androidx.core.util.forEach
import androidx.core.util.putAll
import com.arkivanov.decompose.Child
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.router.pages.ChildPages
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.observe
import com.arkivanov.essenty.lifecycle.*
import com.example.navigation.view.AnimationBehaviour
import com.example.navigation.view.HostView
import com.example.navigation.view.ViewRender

@OptIn(ExperimentalDecomposeApi::class)
class PagesHostView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : HostView(context, attrs, defStyleAttr) {

    private var currentPages: ChildPages<*, *>? = null

    fun <C : Any, T : ViewRender> observe(
        pages: Value<ChildPages<C, T>>,
        hostViewLifecycle: Lifecycle, // view lifecycle
        animationBehaviour: AnimationBehaviour? = null,
    ) {
        this.animationBehaviour = animationBehaviour
        hostViewLifecycle.doOnDestroy { animator?.end() }
        pages.observe(hostViewLifecycle) {
            onPagesChanged(it, hostViewLifecycle)
        }
    }

    private fun <C : Any, T : ViewRender> onPagesChanged(
        pages: ChildPages<C, T>,
        hostViewLifecycle: Lifecycle,
    ) {
        animator?.end()
        @Suppress("UNCHECKED_CAST")
        val currentPages = currentPages as ChildPages<C, T>?

        @Suppress("UNCHECKED_CAST")
        val currentChild = currentChild as ActiveChild<C, T>?

        val selectedChild = pages.items[pages.selectedIndex] as Child.Created<C, T>

        if (currentChild?.child?.configuration != selectedChild.configuration) {

            val activeChild = createActiveChild(hostViewLifecycle, selectedChild)
            this.addView(activeChild.view)

            if (isInPages(pages, currentChild)) {
                addActiveToInactive(currentChild)
            }
            validateInactive(pages)
            val animation = hasAnimation(currentChild, activeChild)
            // Нужно анимировать если уже есть view и указана анимация
            if (animation != null && currentChild != null) {
                animateChange(currentChild, activeChild, animation) {
                    switchCurrent(currentChild, activeChild, pages)
                }
            } else {
                switchCurrent(currentChild, activeChild, pages)
            }
        } else {
            validateInactive(pages)
        }
    }

    private fun switchCurrent(current: ActiveChild<*, *>?, active: ActiveChild<*, *>, pages: ChildPages<*, *>) {
        current?.lifecycle?.destroy()
        active.lifecycle.resume()
        this.removeView(currentChild?.view)
        this.currentChild = active
        this.currentPages = pages
    }

    private fun hasAnimation(
        current: ActiveChild<*, *>?,
        active: ActiveChild<*, *>,
    ): AnimationBehaviour?{
        if (current == null) return null
        return active.animationBehaviour
    }

    private fun animateChange(
        current: ActiveChild<*, *>,
        active: ActiveChild<*, *>,
        animation: AnimationBehaviour,
        onEnd: () -> Unit
    ) {
        animator = animation.open(current.view, active.view, this)
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

    private fun isInPages(pages: ChildPages<*, *>?, child: ActiveChild<*, *>?): Boolean{
        return pages != null &&
                child != null &&
                pages.items.any { it.configuration == child.child.configuration }
    }

    // Синхронизируем не активные элемнеты с backstack.
    private fun validateInactive(pages: ChildPages<*, *>) {
        val validChild = SparseArray<InactiveChild>()
        val validKeys = pages.items.asSequence().mapIndexedNotNull { index, child ->
            if (index != pages.selectedIndex) child.getKey() else null
        }.toSet()
        inactiveChildren.forEach { key, child ->
            if (key in validKeys){
                validChild.put(key, child)
            }
        }
        inactiveChildren.clear()
        inactiveChildren.putAll(validChild)
    }
}
