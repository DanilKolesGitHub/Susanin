package com.example.navigation.root

import android.animation.Animator
import android.animation.AnimatorSet
import android.content.Context
import android.util.AttributeSet
import com.arkivanov.decompose.value.ObserveLifecycleMode
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.observe
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.destroy
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.arkivanov.essenty.lifecycle.pause
import com.arkivanov.essenty.lifecycle.resume
import com.arkivanov.essenty.lifecycle.start
import com.example.navigation.view.HostView
import com.example.navigation.view.UiParams
import com.example.navigation.view.ViewRender

class RootHostView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : HostView(context, attrs, defStyleAttr) {

    private var currentRoot: ChildRoot<*, *>? = null
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
        root: Value<ChildRoot<C, T>>,
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
        root.observe(hostViewLifecycle, ObserveLifecycleMode.START_STOP) {
            onRootChanged(it, hostViewLifecycle)
        }
    }

    /**
     * Обновляет slot.
     *
     * @param slot Новый ChildSlot
     * @param hostViewLifecycle ЖЦ родительской View
     */
    private fun <C : Any, T : ViewRender> onRootChanged(
        root: ChildRoot<C, T>,
        hostViewLifecycle: Lifecycle,
    ) {
        endTransition() // Останавливаем анимацию прошлого изменения.

        @Suppress("UNCHECKED_CAST")
        val currentRoot = currentRoot as ChildRoot<C, T>?

        @Suppress("UNCHECKED_CAST")
        val currentChild = currentChild as ActiveChild<C, T>?

        if (currentChild?.child?.configuration != root.child.configuration) {
            // Создаем новый активный экран.
            val activeChild = createActiveChild(hostViewLifecycle, root.child)
            this.currentChild = activeChild
            this.currentRoot = root
            if (currentChild == null) {
                // Предыдущего не было. Просто добавляем новый экран.
                addView(activeChild.view)
                activeChild.lifecycle.resume()
            } else {
                // Анимируем изменения. Или нет если нет анимации.
                // Во время анимации текущая и новая view в состоянии STARTED.
                // По окончании анимации новая RESUMED, а текущая DESTROYED.
                beginTransition(
                    addToBack = false,
                    add = activeChild,
                    remove = currentChild,
                    animatorProvider = {
                        provideTransition(currentChild, activeChild)
                    },
                    onStart = {
                        currentChild.lifecycle.pause()
                        activeChild.lifecycle.start()
                    },
                    onEnd = {
                        currentChild.lifecycle.destroy()
                        activeChild.lifecycle.resume()
                    },
                )
            }
        }
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
    ): Animator? {
        val currentTransition = current?.viewTransition?.exitThis(current.view, this)
        val activeTransition = active?.viewTransition?.enterThis(active.view, this)
        return when {
            currentTransition != null && activeTransition == null -> currentTransition
            currentTransition == null && activeTransition != null -> activeTransition
            currentTransition != null && activeTransition != null -> AnimatorSet().apply {
                playTogether(currentTransition, activeTransition)
            }
            else -> null
        }
    }
}
