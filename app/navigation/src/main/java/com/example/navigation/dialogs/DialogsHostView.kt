package com.example.navigation.dialogs

import android.animation.Animator
import android.animation.AnimatorSet
import android.content.Context
import android.util.AttributeSet
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

class DialogsHostView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : HostView(context, attrs, defStyleAttr) {

    private var currentDialogs: ChildDialogs<*, *>? = null

    /**
     * Активные экраны/view, которые добавлены в DialogsHostView.
     * Все view в состоянии CREATED/STARTED, кроме последнего/верхнего он в RESUMED.
     */
    private var currentChildren = listOf<ActiveChild<*, *>>()

    /**
     * Сохраняем состояния всех активных view.
     */
    override fun saveActive() {
        currentChildren.forEach {
            saveActive(it)
        }
    }

    /**
     * Восстанавливаем состояния всех активных view.
     */
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
        endTransition() // останавливаем анимацию
        @Suppress("UNCHECKED_CAST")
        val currentDialogs = currentDialogs as ChildDialogs<C, T>?
        @Suppress("UNCHECKED_CAST")
        val currentChild = currentChildren.lastOrNull() as ActiveChild<C, T>?

        if (currentDialogs == dialogs) return
        val (activeChildren, insertedChildren, removedChildren) = createActiveChildren(hostViewLifecycle, dialogs)
        // Анимируем изменения. Или нет если нет анимации.
        // Во время анимации текущая и новая view в состоянии STARTED.
        // По окончании анимации новая RESUMED, а текущая DESTROYED.
        beginTransition(
            addToBack = false,
            addChildren = activeChildren,
            removeChildren = removedChildren,
            animatorProvider = { provideTransition(removedChildren, insertedChildren) },
            onStart = {
                removedChildren.map(ActiveChild<*,*>::lifecycle).forEach(LifecycleRegistry::pause)
                currentChild?.lifecycle?.pause()
                activeChildren.map(ActiveChild<*,*>::lifecycle).forEach(LifecycleRegistry::start)
            },
            onEnd = {
                removedChildren.map(ActiveChild<*,*>::lifecycle).forEach(LifecycleRegistry::destroy)
                activeChildren.lastOrNull()?.lifecycle?.resume()
            },
        )
        this.currentChildren = activeChildren
        this.currentDialogs = dialogs
        validateInactive(null)
    }

    /**
     * Создаем активные экраны для новых диалогов.
     * Причем если view экрана уже есть, то она переиспользуется.
     *
     * @return
     * activeChildren - список всех view, которые должны, быть в DialogsHostView. Причем некоторые из них могут быть уже добавлены.
     * insertedChildren - список всех view, которые были созданы.
     * removedChildren - список всех view, которые должны быть удалены из DialogsHostView.
     */
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
     * Для каждого экрана берется enterThis или exitThis анимация.
     *
     * @param removedChildren Удаляемые экраны.
     * @param insertedChildren Добавленные экраны.
     */
    private fun provideTransition(
        removedChildren: Collection<ActiveChild<*, *>>,
        insertedChildren: Collection<ActiveChild<*, *>>,
    ): Animator? {
        val animations = LinkedList<Animator?>()
        insertedChildren.asSequence()
            .forEach { animations += it.viewTransition?.enterThis(it.view, this) }
        removedChildren.asSequence()
            .forEach { animations += it.viewTransition?.exitThis(it.view, this) }
        return animations.filterNotNull().run {
            if (isNotEmpty()) {
                AnimatorSet().also { it.playTogether(this) }
            } else {
                null
            }
        }
    }
}
