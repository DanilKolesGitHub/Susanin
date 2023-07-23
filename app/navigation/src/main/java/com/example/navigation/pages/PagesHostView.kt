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
import com.example.navigation.view.TransitionProvider
import com.example.navigation.view.ViewRender
import com.example.navigation.view.addCallbacks

@OptIn(ExperimentalDecomposeApi::class)
class PagesHostView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : HostView(context, attrs, defStyleAttr) {

    private var currentPages: ChildPages<*, *>? = null

    /**
     * Подписывается на изменение ChildPages<C, T> и отрисовывает View.
     * T должен наследовать ViewRender.
     * С должен быть Parcelable или data class, data object.
     * Необходимо, поскольку является ключем для сохранения состояния View.
     *
     * @param pages Источник ChildPages
     * @param hostViewLifecycle Родительский ЖЦ в котором находится PagesHostView.
     * Нужен для создания дочерних view. Если умирает, то и все дочерние тоже умирают.
     * @param transitionProvider Анимация изменений в PagesHostView.
     */
    fun <C : Any, T : ViewRender> observe(
        pages: Value<ChildPages<C, T>>,
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
        val currentChild = currentChild as ActiveChild<C, T>?

        val selectedChild = pages.items[pages.selectedIndex] as Child.Created<C, T>

        if (currentChild?.child?.configuration != selectedChild.configuration) {
            // Создаем новый активный экран.
            val activeChild = createActiveChild(hostViewLifecycle, selectedChild)
            if (currentChild == null) {
                // Предыдущего не было. Просто добавляем новый экран.
                addView(activeChild.view)
                activeChild.lifecycle.resume()
                this.currentChild = activeChild
                this.currentPages = pages
            } else {
                if (isInPages(pages, currentChild)) {
                    // Если текущий экран остался в pages сохраняем его состояние.
                    addActiveToInactive(currentChild)
                }
                // Анимируем изменения. Или нет если нет анимации.
                // Во время анимации текущая и новая view в состоянии STARTED.
                // По окончании анимации новая RESUMED, а текущая DESTROYED.
                beginTransition(provideTransition(currentChild, activeChild),
                    onStart = {
                        activeChild.lifecycle.start()
                        currentChild.lifecycle.pause()
                    },
                    onEnd = {
                        currentChild.lifecycle.destroy()
                        activeChild.lifecycle.resume()
                        this.currentChild = activeChild
                        this.currentPages = pages
                    },
                    changes = {
                        removeView(currentChild.view)
                        addView(activeChild.view)
                    }
                )
            }
        }
        validateInactive(pages)
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
        val backView = current.view
        transition.addTarget(animatedView)
        startViewTransition(backView)
        transition.addCallbacks(onEnd = { endViewTransition(backView) })
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
