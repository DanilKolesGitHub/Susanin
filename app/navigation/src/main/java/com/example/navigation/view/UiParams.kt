package com.example.navigation.view

import android.animation.Animator
import android.animation.ObjectAnimator
import android.os.Parcelable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import androidx.transition.Fade
import androidx.transition.Slide
import androidx.transition.Transition
import kotlinx.android.parcel.Parcelize

interface UiParams {
    val transition: Transition? get() = null
    val overlay: Boolean get() = false
    val viewTransition: ViewTransition? get() = null
}

/**
 * Описывает анимации перехода между View экранами.
 * Используется в HostView контэйнерах при изменении состояний навигации.
 *
 * Поскольку ViewTransition можно указать для конкретного экрана или параметов этого экрана.
 * Можно сказать что анимация специфична для view этого экрана.
 * Поэтому для анимации добавления/удаления этого экрана используйте методы enterThis/exitThis.
 * Если необходимо санимровать предыдущую view используте enterOther/exitOther.
 *
 * Учтите что HostView имеют специфику и по-разному используют это api.
 *
 * Slot, Dialogs:
 * Для добавления экрана вызывается enterThis, а для удаления вызывается exitThis.
 * Методы enterOther и exitOther не используются.
 * То есть если был экран A и его сменяет экран B. Будут вызваны A.exitThis(aView) и B.enterThis(bView)
 *
 * Stack, Pages:
 * При показе следующего верхнего экрана A -> B.
 * Для текущего вызывается B.enterThis(bView), для предыдущего B.exitOther(aView).
 * При возврате на предыдущий A <- B.
 * Для текущего вызывается B.exitThis(bView), для предыдущего B.enterOther(aView).
 */
interface ViewTransition {
    /**
     * Анимация добавления view экрана.
     * @param view Добавляемая view экрана.
     * @param parent HostView, в который добавляется view.
     * @return Анимация. Если null, view появится сразу.
     */
    fun enterThis(view: View, parent: ViewGroup): Animator? = null
    /**
     * Анимация удаления view экрана.
     * @param view Удаляемая view экрана.
     * @param parent HostView, из которого удаляется view.
     * @return Анимация. Если null, view появится сразу.
     */
    fun exitThis(view: View, parent: ViewGroup): Animator? = null
    /**
     * Анимация добавления предыдущего view экрана.
     * @param view Добавляемая view экрана.
     * @param parent HostView, в который добавляется view.
     * @return Анимация. Если null, view появится сразу.
     */
    fun enterOther(view: View, parent: ViewGroup): Animator? = null
    /**
     * Анимация удаления view предыдущего экрана.
     * @param view Удаляемая view экрана.
     * @param parent HostView, из которого удаляется view.
     * @return Анимация. Если null, view появится сразу.
     */
    fun exitOther(view: View, parent: ViewGroup): Animator? = null
}

object FadeViewTransition: ViewTransition {
    override fun enterThis(view: View, parent: ViewGroup): Animator? {
        return ObjectAnimator.ofFloat(view, View.ALPHA, 0f, 1f).apply {
            duration = DURATION
        }
    }
    override fun exitThis(view: View, parent: ViewGroup): Animator? {
        return ObjectAnimator.ofFloat(view, View.ALPHA, 1f, 0f).apply {
            duration = DURATION
        }
    }
}

val DURATION = 3000L

object SlideViewTransition: ViewTransition {
    override fun enterThis(view: View, parent: ViewGroup): Animator? {
        return ObjectAnimator.ofFloat(view, View.TRANSLATION_X, view.width.toFloat(), 0f).apply {
            duration = DURATION
        }
    }
    override fun exitThis(view: View, parent: ViewGroup): Animator? {
        return ObjectAnimator.ofFloat(view, View.TRANSLATION_X, 0f, view.width.toFloat()).apply {
            duration = DURATION
        }
    }

    override fun enterOther(view: View, parent: ViewGroup): Animator? {
        return ObjectAnimator.ofFloat(view, View.TRANSLATION_X, -view.width.toFloat(), 0f).apply {
            duration = DURATION
        }
    }

    override fun exitOther(view: View, parent: ViewGroup): Animator? {
        return ObjectAnimator.ofFloat(view, View.TRANSLATION_X, 0f, -view.width.toFloat()).apply {
            duration = DURATION
        }
    }
}

@Parcelize
object TopTransition: UiParams, Parcelable {
    override val transition: Transition get() = Fade()
    override val overlay: Boolean get() = false
}

@Parcelize
object ForwardBackwardTransition: UiParams, Parcelable {
    override val transition: Transition get() = Slide(Gravity.END)
    override val overlay: Boolean get() = false
}

@Parcelize
object BottomUpTransition: UiParams, Parcelable {
    override val transition: Transition get() = Slide(Gravity.BOTTOM)
    override val overlay: Boolean get() = false
}

@Parcelize
object OverlayTransition: UiParams, Parcelable {
    override val transition: Transition get() = Slide(Gravity.END)
    override val overlay: Boolean get() = true
}