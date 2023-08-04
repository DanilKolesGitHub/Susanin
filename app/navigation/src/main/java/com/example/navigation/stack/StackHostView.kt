package com.example.navigation.stack

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import androidx.transition.Transition
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.ObserveLifecycleMode
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.observe
import com.arkivanov.essenty.lifecycle.*
import com.example.navigation.view.*

class StackHostView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : HostView(context, attrs, defStyleAttr) {

    private var currentStack: ChildStack<*, *>? = null

    /**
     * Подписывается на изменение ChildStack<C, T> и отрисовывает View.
     * T должен наследовать ViewRender.
     * С должен быть Parcelable или data class, data object.
     * Необходимо, поскольку является ключем для сохранения состояния View.
     *
     * @param stack Источник ChildStack
     * @param hostViewLifecycle Родительский ЖЦ в котором находится StackHostView.
     * Нужен для создания дочерних view. Если умирает, то и все дочерние тоже умирают.
     * @param transitionProvider Анимация изменений в StackHostView.
     */
    fun <C : Any, T : ViewRender> observe(
        stack: Value<ChildStack<C, T>>,
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
        stack.observe(hostViewLifecycle, ObserveLifecycleMode.START_STOP) {
            onStackChanged(it, hostViewLifecycle)
        }
    }

    private fun <C : Any, T : ViewRender> onStackChanged(
        stack: ChildStack<C, T>,
        hostViewLifecycle: Lifecycle,
    ) {
        endTransition()
        @Suppress("UNCHECKED_CAST")
        val currentStack = currentStack as ChildStack<C, T>?

        @Suppress("UNCHECKED_CAST")
        val currentChild = currentChild as ActiveChild<C, T>?

        Log.d("SERDEB", this.hashCode().toString())
        stack.items.forEach { Log.d("SERDEB", it.toString()) }

        if (currentChild?.child?.configuration != stack.active.configuration) {
            // Создаем новый активный экран.
            val activeChild = createActiveChild(hostViewLifecycle, stack.active)
            if (currentChild == null) {
                // Предыдущего не было. Просто добавляем новый экран.
                addView(activeChild.view)
                activeChild.lifecycle.resume()
                this.currentChild = activeChild
                this.currentStack = stack
            } else {
                if (isInBackStack(stack, currentChild)) {
                    // Если текущий экран остался в stack сохраняем его состояние.
                    addActiveToInactive(currentChild)
                }
                // Новый экран был в стеке, поэтому проигрываем анимацию текущего в обратную сторону.
                val activeFromStack = isInBackStack(currentStack, activeChild)
                // Анимируем изменения. Или нет если нет анимации.
                // Во время анимации текущая и новая view в состоянии STARTED.
                // По окончании анимации новая RESUMED, а текущая DESTROYED.
                beginTransition(provideTransition(currentChild, activeChild, activeFromStack),
                    onStart = {
                        activeChild.lifecycle.start()
                        currentChild.lifecycle.pause()
                    },
                    onEnd = {
                        currentChild.lifecycle.destroy()
                        activeChild.lifecycle.resume()
                    },
                    changes = {
                        removeView(currentChild.view)
                        addView(activeChild.view)
                    }
                )
                this.currentChild = activeChild
                this.currentStack = stack
            }
        }
        validateInactive(stack)
    }

    /**
     * Предоставляет анимацию.
     * Если предыдущего экрана не было, то не анимируем появление первого экрана.
     * Если новый эран из стека, то проигрываем анимацию удатения текущего.
     * Иначе проигрываем анимацию добавления нового.
     *
     * @param current Текущий экран.
     * @param active Новый экран.
     */
    private fun provideTransition(
        current: ActiveChild<*, *>,
        active: ActiveChild<*, *>,
        back: Boolean
    ): Transition? {
        val transition = if (back) current.transition else active.transition
        transition ?: return null
        val animatedView = if (back) current.view else active.view
        val backView = if (back) active.view else current.view
        transition.addTarget(animatedView)
        startViewTransition(backView)
        transition.addCallbacks(onEnd = { endViewTransition(backView) })
        return transition
    }

    /**
     * Проверяет нахождение child в stack.
     * Сравнивает по Сhild.configuration
     */
    private fun isInBackStack(stack: ChildStack<*, *>?, child: ActiveChild<*, *>): Boolean{
        return stack != null &&
                stack.backStack.any { it.configuration == child.child.configuration }
    }

    /**
     * Удаляет все ранее сохраненные состаяния эранов, если они больше не находятся в stack.
     *
     * @param stack Новый ChildStack
     */
    private fun validateInactive(stack: ChildStack<*, *>) {
        // Собираем все key из нового stack. Берем только не активные экраны.
        val validKeys = stack.backStack.toMutableSet()
        validKeys.add(stack.active)
        // Сохраняем в inactiveChildren только те, которые находятся в новом stack.
        validateInactive(validKeys)
    }
}
