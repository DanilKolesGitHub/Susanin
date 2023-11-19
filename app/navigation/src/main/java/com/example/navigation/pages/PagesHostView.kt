package com.example.navigation.pages

import android.content.Context
import android.util.AttributeSet
import androidx.transition.Transition
import com.arkivanov.decompose.Child
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.router.pages.ChildPages
import com.arkivanov.decompose.value.ObserveLifecycleMode
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.observe
import com.arkivanov.essenty.lifecycle.*
import com.example.navigation.view.HostView
import com.example.navigation.view.UiParams
import com.example.navigation.view.ViewRender
import com.example.navigation.view.addCallbacks
import java.util.LinkedList

@OptIn(ExperimentalDecomposeApi::class)
class PagesHostView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : HostView(context, attrs, defStyleAttr) {

    private var currentPages: ChildPages<*, *>? = null

    /**
     * Активные экраны/view, которые добавлены в PagesHostView.
     * Все view в состоянии STARTED, кроме выбранного он в RESUMED.
     */
    private var currentChildren = listOf<ActiveChild<*, *>>()

    /**
     * Сохраняем состояния всх активных view.
     */
    override fun saveActive() {
        currentChildren.forEach {
            saveActive(it)
        }
    }

    /**
     * Восстанавливаем состояния всх активных view.
     */
    override fun restoreActive() {
        currentChildren.forEach {
            restoreActive(it)
        }
    }


    /**
     * Подписывается на изменение ChildPages<C, T> и отрисовывает View.
     * T должен наследовать ViewRender.
     * С должен быть Parcelable или data class, data object.
     * Необходимо, поскольку является ключем для сохранения состояния View.
     *
     * @param pages Источник ChildPages
     * @param hostViewLifecycle Родительский ЖЦ в котором находится PagesHostView.
     * Нужен для создания дочерних view. Если умирает, то и все дочерние тоже умирают.
     * @param uiParams Анимация изменений в PagesHostView.
     */
    fun <C : Any, T : ViewRender> observe(
        pages: Value<ChildPages<C, T>>,
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
        pages.observe(hostViewLifecycle, ObserveLifecycleMode.START_STOP) {
            onPagesChanged(it, hostViewLifecycle)
        }
    }

    /**
     * Обновляет pages.
     *
     * @param pages Новый ChildPages
     * @param hostViewLifecycle ЖЦ родительской View
     */
    private fun <C : Any, T : ViewRender> onPagesChanged(
        pages: ChildPages<C, T>,
        hostViewLifecycle: Lifecycle,
    ) {
        endTransition() // Останавливаем анимацию прошлого изменения.

        @Suppress("UNCHECKED_CAST")
        val currentPages = currentPages as ChildPages<C, T>?
        @Suppress("UNCHECKED_CAST")
        val currentChild = currentChildren.lastOrNull() as ActiveChild<C, T>?

        if (currentPages == pages) return

        val (activeChildren, removedChildren) = createActiveChildren(hostViewLifecycle, pages)
        val activeChild = activeChildren.last() // Новый выбранный экран.
        if (currentChild == null) {
            // Предыдущего не было. Просто добавляем новые экраны.
            activeChildren.forEach {
                addView(it.view)
                it.lifecycle.start()
            }
            // Выбранный является активным, переводим его в RESUMED.
            activeChild.lifecycle.resume()
        } else {
            removedChildren.forEach {
                // Если текущий экран остался в pages сохраняем его состояние.
                if (isInPages(pages, it)) {
                    saveActive(it)
                }
            }
            beginTransition(provideTransition(currentChild, activeChild),
                onStart = {
                    activeChildren.map(ActiveChild<*,*>::lifecycle).forEach(LifecycleRegistry::start)
                    removedChildren.map(ActiveChild<*,*>::lifecycle).forEach(LifecycleRegistry::pause)
                },
                onEnd = {
                    removedChildren.map(ActiveChild<*,*>::lifecycle).forEach(LifecycleRegistry::destroy)
                    currentChild.lifecycle.pause()
                    activeChild.lifecycle.resume()
                },
                changes = {
                    removedChildren.map(ActiveChild<*,*>::view).forEach(::removeView)
                    val currentIndex = indexOfChild(activeChild.view)
                    activeChildren.take(activeChildren.lastIndex).forEach {
                        it.view.visibility = GONE
                    }
                    activeChild.view.visibility = VISIBLE
                    if (currentIndex >= 0) {
                        removeView(activeChild.view)
                        addView(activeChild.view)
                    } else {
                        addView(activeChild.view)
                    }
                }
            )
        }
        this.currentChildren = activeChildren
        this.currentPages = pages
        validateInactive(pages)
    }

    /**
     * Создаем активные экраны для нового pages.
     * Причем если view экрана уже есть, то она переиспользуется.
     *
     * Тут используется свойство overlay.
     * Если у экрана overlay == true, то он работает по принципу add.
     * Если у экрана overlay == false, то он работает по принципу replace.
     * @see UiParams.overlay
     *
     * В активные экраны попадает выбранный экран.
     * Если он или его параметры определяют overlay == true, то в активные элементы попадает и текущие экраны.
     * Если overlay == false, то все текущие экраны удаляются.
     *
     * @return
     * activeChildren - список всех view, которые должны, быть в PagesHostView. Причем некоторые из них могут быть уже добавлены.
     * removedChildren - список всех view, которые должны быть удалены из PagesHostView.
     *
     */
    private fun<C : Any, T : ViewRender> createActiveChildren(
        hostViewLifecycle: Lifecycle,
        pages: ChildPages<C, T>,
    ): Pair<List<ActiveChild<C, T>>, Collection<ActiveChild<C, T>>> {
        val activeChildren = LinkedList<ActiveChild<C, T>>()
        val removedChildren = LinkedList<ActiveChild<C, T>>()
        val currentMap = currentChildren.associateByTo(LinkedHashMap(), ActiveChild<*,*>::id) { it as ActiveChild<C, T> }
        // Создаем или переиспользуем выбранный экран.
        val selectedItem = pages.items[pages.selectedIndex] as Child.Created<C, T>
        val selectedChild = currentMap.remove(selectedItem.id()) ?: createActiveChild(hostViewLifecycle, selectedItem)
        if (selectedChild.overlay) {
            // Id всех экранов в pages.
            val ids = pages.items.asSequence().map { it.id() }.toHashSet()
            currentMap.values.forEach {
                if (it.id in ids) { // Экран содержится в новом pages, оставляем его.
                    activeChildren.add(it)
                } else { // Экрана нет в новом pages, удаляем его.
                    removedChildren.add(it)
                }
            }
        } else { // Экран overlay == false, удаляем все экраны под ним.
            removedChildren.addAll(currentMap.values)
        }
        activeChildren.add(selectedChild)
        return Pair(activeChildren, removedChildren)
    }

    /**
     * Предоставляет анимацию.
     * Если предыдущего экрана не было, то не анимируем появление первого экрана.
     *
     * @param current Текущий экран.
     * @param active Новый экран.
     */
    private fun provideTransition(
        current: ActiveChild<*, *>,
        active: ActiveChild<*, *>,
    ): Transition? {
        val transition = active.transition ?: return null
        val animatedView = active.view
        transition.addTarget(animatedView)
//        if (!active.overlay) {
            val backView = current.view
            startViewTransition(backView)
            transition.addCallbacks(onEnd = { endViewTransition(backView) })
//        }
        transition.duration = 1000
        return transition
    }

    /**
     * Проверяет нахождение child в pages.
     * Сравнивает по Сhild.configuration
     */
    private fun isInPages(pages: ChildPages<*, *>, child: ActiveChild<*, *>): Boolean{
        return  pages.items.any { it.configuration == child.child.configuration }
    }

    /**
     * Удаляет все ранее сохраненные состаяния эранов, если они больше не находятся в pages.
     *
     * @param pages Новый ChildPages
     */
    private fun validateInactive(pages: ChildPages<*, *>) {
        // Сохраняем в inactiveChildren только те, которые находятся в новом pages.
        validateInactive(pages.items)
    }
}
