package com.example.navigation.slot

import android.animation.Animator
import android.animation.AnimatorSet
import android.content.Context
import android.os.Parcelable
import android.util.AttributeSet
import android.view.ViewGroup
import androidx.core.animation.addListener
import androidx.core.animation.doOnStart
import androidx.transition.Transition
import androidx.transition.TransitionManager
import androidx.transition.TransitionSet
import androidx.transition.TransitionSet.ORDERING_SEQUENTIAL
import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.value.ObserveLifecycleMode
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.observe
import com.arkivanov.essenty.lifecycle.*
import com.example.navigation.view.*

class SlotHostView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : HostView(context, attrs, defStyleAttr) {

    private var currentSlot: ChildSlot<*, *>? = null
    // В отличии от Stack и Pages анимирует открытие первого экрана.
    // Поэтому чтобы не анимировать ранее открытый экран после восстанавления состояния,
    // используется флаг.
    private var afterRestore: Boolean = false

    /**
     * Подписывается на изменение ChildSlot<C, T> и отрисовывает View.
     * T должен наследовать ViewRender.
     *
     * @param slot Источник ChildSlot
     * @param hostViewLifecycle Родительский ЖЦ в котором находится SlotHostView.
     * Нужен для создания дочерних view. Если умирает, то и все дочерние тоже умирают.
     * @param transitionProvider Анимация изменений в SlotHostView.
     */
    fun <C : Any, T : ViewRender> observe(
        slot: Value<ChildSlot<C, T>>,
        hostViewLifecycle: Lifecycle,
        transitionProvider: TransitionProvider? = null,
    ) {
        this.transitionProvider = transitionProvider
        // Если родитель умирает останавливаем анимацию.
        hostViewLifecycle.doOnDestroy { endTransition() }
        // Реагируем на изменения только в состоянии STARTED и выше.
        // Поскольку в этом состоянии находится View во время анимации.
        // Если поднять до RESUMED, то экран анимирует свое открытие и только потом отрисует содержимое.
        // Те во время анимации будет пустым.
        slot.observe(hostViewLifecycle, ObserveLifecycleMode.START_STOP) {
            onSlotChanged(it, hostViewLifecycle)
        }
    }

    /**
     * Обновляет slot.
     *
     * @param slot Новый ChildSlot
     * @param hostViewLifecycle ЖЦ родительской View
     */
    private fun <C : Any, T : ViewRender> onSlotChanged(
        slot: ChildSlot<C, T>,
        hostViewLifecycle: Lifecycle,
    ) {
        endTransition() // Останавливаем анимацию прошлого изменения.

        @Suppress("UNCHECKED_CAST")
        val currentSlot = currentSlot as ChildSlot<C, T>?

        @Suppress("UNCHECKED_CAST")
        val currentChild = currentChild as ActiveChild<C, T>?

        if (currentChild?.child?.configuration != slot.child?.configuration) {
            // Создаем новый активный экран.
            val activeChild = slot.child?.let {
                createActiveChild(hostViewLifecycle, it)
            }
            // Анимируем изменения. Или нет если нет анимации.
            // Во время анимации текущая и новая view в состоянии STARTED.
            // По окончании анимации новая RESUMED, а текущая DESTROYED.
            beginTransition(provideTransition(currentChild, activeChild),
                onStart = {
                    activeChild?.lifecycle?.start()
                    currentChild?.lifecycle?.pause()
                },
                onEnd = {
                    currentChild?.lifecycle?.destroy()
                    activeChild?.lifecycle?.resume()
                    this.currentChild = activeChild
                    this.currentSlot = slot
                }
            )
            currentChild?.view?.let(::removeView)
            activeChild?.view?.let(::addView)
        }
        clearInactive()
        afterRestore = false
    }

    /**
     * Предоставляет анимацию.
     * Для каждого экрана использует соответствующую анимацию открытия и закрытия.
     * Конечно если экраны существуют.
     *
     * @param current Текущий экран.
     * @param active Новый экран.
     */
    private fun provideTransition(
        current: ActiveChild<*, *>?,
        active: ActiveChild<*, *>?,
    ): Transition? {
        if (afterRestore) return null
        val currentTransition = current?.let {
            it.transition?.addTarget(it.view)
        }
        val activeTransition = active?.let {
            it.transition?.addTarget(it.view)
        }
        return when {
            currentTransition != null && activeTransition == null -> currentTransition
            currentTransition == null && activeTransition != null -> activeTransition
            currentTransition != null && activeTransition != null ->
                TransitionSet()
                    .setOrdering(ORDERING_SEQUENTIAL)
                    .addTransition(currentTransition)
                    .addTransition(activeTransition)
            else -> null
        }
    }

    private fun clearInactive() {
        // Удаляем сохранненные состояния.
        inactiveChildren.clear()
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        afterRestore = state != null
        super.onRestoreInstanceState(state)
    }
}
