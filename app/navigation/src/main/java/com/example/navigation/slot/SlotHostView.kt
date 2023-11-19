package com.example.navigation.slot

import android.content.Context
import android.util.AttributeSet
import androidx.transition.Transition
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
    private var currentChild: ActiveChild<*, *>? = null

    override fun saveActive() {
        saveActive(currentChild)
    }

    override fun restoreActive() {
        restoreActive(currentChild)
    }

    /**
     * Подписывается на изменение ChildSlot<C, T> и отрисовывает View.
     * T должен наследовать ViewRender.
     * С должен быть Parcelable или data class, data object.
     * Необходимо, поскольку является ключем для сохранения состояния View.
     *
     * @param slot Источник ChildSlot
     * @param hostViewLifecycle Родительский ЖЦ в котором находится SlotHostView.
     * Нужен для создания дочерних view. Если умирает, то и все дочерние тоже умирают.
     * @param uiParams Анимация изменений в SlotHostView.
     */
    fun <C : Any, T : ViewRender> observe(
        slot: Value<ChildSlot<C, T>>,
        hostViewLifecycle: Lifecycle,
        uiParams: UiParams? = null,
    ) {
        this.uiParams = uiParams
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

        if (currentSlot == slot) return

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
            },
            changes = {
                currentChild?.view?.let(::removeView)
                activeChild?.view?.let(::addView)
            }
        )
        this.currentChild = activeChild
        this.currentSlot = slot
        validateInactive(slot)
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

    /**
     * Удаляет все ранее сохраненные состаяния эранов, если они больше не находятся в slot.
     *
     * @param slot Новый ChildSlot
     */
    private fun validateInactive(slot: ChildSlot<*, *>) {
        validateInactive(if (slot.child != null) setOf(slot.child!!) else setOf())
    }
}
