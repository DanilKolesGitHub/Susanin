package com.example.navigation.dialogs

import android.content.Context
import android.util.AttributeSet
import androidx.transition.Transition
import androidx.transition.TransitionSet
import com.arkivanov.decompose.value.ObserveLifecycleMode
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.observe
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.destroy
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.arkivanov.essenty.lifecycle.pause
import com.arkivanov.essenty.lifecycle.resume
import com.arkivanov.essenty.lifecycle.start
import com.example.navigation.view.HostView
import com.example.navigation.view.UiParams
import com.example.navigation.view.ViewRender
import java.util.LinkedList

class DialogsHostView2 @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : HostView(context, attrs, defStyleAttr) {

    private var currentChildren = listOf<ActiveChild<*, *>>()
    private var currentDialogs: ChildDialogs<*, *>? = null

    override fun saveActive() {
        currentChildren.forEach {
            saveActive(it)
        }
    }

    override fun restoreActive() {
        currentChildren.forEach {
            restoreActive(it)
        }
    }

    /**
     * Подписывается на изменение ChildDialogs<C, T> и отрисовывает View.
     * T должен наследовать ViewRender.
     * С должен быть Parcelable или data class, data object.
     * Необходимо, поскольку является ключем для сохранения состояния View.
     *
     * @param dialogs Источник ChildDialogs
     * @param hostViewLifecycle Родительский ЖЦ в котором находится DialogsHostView.
     * Нужен для создания дочерних view. Если умирает, то и все дочерние тоже умирают.
     * @param uiParams Анимация изменений в DialogsHostView.
     */
    fun <C : Any, T : ViewRender> observe(
        dialogs: Value<ChildDialogs<C, T>>,
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
        dialogs.observe(hostViewLifecycle, ObserveLifecycleMode.START_STOP) {
            onDialogsChanged(it, hostViewLifecycle)
        }
    }

    private fun <C : Any, T : ViewRender> onDialogsChanged(
        dialogs: ChildDialogs<C, T>,
        hostViewLifecycle: Lifecycle,
    ) {
        endTransition()
        @Suppress("UNCHECKED_CAST")
        val currentDialogs = currentDialogs as ChildDialogs<C, T>?
        @Suppress("UNCHECKED_CAST")
        val currentChild = currentChildren.lastOrNull() as ActiveChild<C, T>?

        if (currentDialogs == dialogs) return
        val (activeChildren, insertedChildren, removedChildren) = createActiveChildren(hostViewLifecycle, dialogs)
        // Анимируем изменения. Или нет если нет анимации.
        // Во время анимации текущая и новая view в состоянии STARTED.
        // По окончании анимации новая RESUMED, а текущая DESTROYED.
        beginTransition(provideTransition(removedChildren, insertedChildren),
            onStart = {
                activeChildren.map(ActiveChild<*,*>::lifecycle).forEach(LifecycleRegistry::start)
                removedChildren.map(ActiveChild<*,*>::lifecycle).forEach(LifecycleRegistry::pause)
            },
            onEnd = {
                removedChildren.map(ActiveChild<*,*>::lifecycle).forEach(LifecycleRegistry::destroy)
                currentChild?.lifecycle?.pause()
                activeChildren.lastOrNull()?.lifecycle?.resume()
            },
            changes = {
                removedChildren.map(ActiveChild<*,*>::view).forEach(::removeView)
                activeChildren.forEachIndexed { index, activeChild ->
                    // добавляем новые вью или двигаем на новую позицию
                    val currentIndex = indexOfChild(activeChild.view)
                    if (currentIndex >= 0) {
                        if (currentIndex != index) {
                            removeViewAt(currentIndex)
                            addView(activeChild.view, index)
                        } // иначе вью на своем месте
                    } else { // иначе добавляем новую вью
                        addView(activeChild.view, index)
                    }
                }
            }
        )
        this.currentChildren = activeChildren
        this.currentDialogs = dialogs
        validateInactive(null)
    }

    private fun<C : Any, T : ViewRender> createActiveChildren(
        hostViewLifecycle: Lifecycle,
        dialogs: ChildDialogs<C, T>,
    ): Triple<List<ActiveChild<C, T>>, Collection<ActiveChild<C, T>>, Collection<ActiveChild<C, T>>> {
        val activeChildren = LinkedList<ActiveChild<C, T>>()
        val insertedChildren = LinkedList<ActiveChild<C, T>>()
        val currentMap = currentChildren.associateByTo(LinkedHashMap(), ActiveChild<*,*>::id) { it as ActiveChild<C, T> }
        dialogs.dialogs.forEach {
            val id = it.id()
            val child = currentMap.remove(id) ?:
                createActiveChild(hostViewLifecycle, it).also(insertedChildren::add)
            activeChildren.add(child)
        }
        val removedChildren = currentMap.values
        return Triple(activeChildren, insertedChildren, removedChildren)
    }

    /**
     * Предоставляет анимацию.
     * Для каждого экрана использует соответствующую анимацию открытия и закрытия.
     * Конечно если экраны существуют.
     *
     * @param current Текущиие экраны, которые нужно закрыть.
     * @param active Новые экраны, которые нужно открыть.
     */
    private fun provideTransition(
        current: Collection<ActiveChild<*, *>>,
        active: Collection<ActiveChild<*, *>>,
    ): Transition? {
        val currentTransition = current.mapNotNull {
            it.transition?.addTarget(it.view)
        }
        val activeTransition = active.mapNotNull {
            it.transition?.addTarget(it.view)
        }
        return if (currentTransition.isEmpty() && activeTransition.isEmpty()) {
            null
        } else {
            TransitionSet().apply {
                ordering = TransitionSet.ORDERING_TOGETHER
                currentTransition.forEach(::addTransition)
                activeTransition.forEach(::addTransition)
            }
        }
    }
}
