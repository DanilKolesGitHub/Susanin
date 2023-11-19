package com.example.navigation.stack

import android.content.Context
import android.util.AttributeSet
import androidx.transition.Transition
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.ObserveLifecycleMode
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.observe
import com.arkivanov.essenty.lifecycle.*
import com.example.navigation.view.*
import java.util.LinkedList

class StackHostView2 @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : HostView(context, attrs, defStyleAttr) {

    private var currentStack: ChildStack<*, *>? = null

    /**
     * Активные экраны/view, которые добавлены в StackHostView.
     * Все view в состоянии STARTED, кроме последнего/верхнего он в RESUMED.
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
     * Подписывается на изменение ChildStack<C, T> и отрисовывает View.
     * T должен наследовать ViewRender.
     * С должен быть Parcelable или data class, data object.
     * Необходимо, поскольку является ключем для сохранения состояния View.
     *
     * @param stack Источник ChildStack
     * @param hostViewLifecycle Родительский ЖЦ в котором находится StackHostView.
     * Нужен для создания дочерних view. Если умирает, то и все дочерние тоже умирают.
     * @param uiParams Анимация изменений в StackHostView.
     */
    fun <C : Any, T : ViewRender> observe(
        stack: Value<ChildStack<C, T>>,
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
        val currentChild = currentChildren.lastOrNull() as ActiveChild<C, T>?

        if (currentStack == stack) return

        val (activeChildren, removedChildren) = createActiveChildren(hostViewLifecycle, stack)
        val activeChild = activeChildren.last() // Новый верхний экран.
        if (currentChild == null) {
            // Предыдущего не было. Просто добавляем новые экраны.
            activeChildren.forEach {
                addView(it.view)
                it.lifecycle.start()
            }
            // Верхний является активным, переводим его в RESUMED.
            activeChild.lifecycle.resume()
        } else {
            saveRemovedChildren(removedChildren, stack)
            // Новый экран был в стеке, поэтому проигрываем анимацию текущего в обратную сторону.
            val activeFromStack = isInBackStack(currentStack, activeChild)
            // Анимируем изменения. Или нет если нет анимации.
            // Во время анимации текущая и новая view в состоянии STARTED.
            // По окончании анимации новая RESUMED, а текущая DESTROYED.
            beginTransition(provideTransition(currentChild, activeChild, activeFromStack),
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
                    activeChildren.forEachIndexed { index, activeChild ->
                        // добавляем новые view или двигаем на новую позицию
                        val currentIndex = indexOfChild(activeChild.view)
                        if (currentIndex >= 0) {
                            if (currentIndex != index) {
                                // двигаем view на новую позицию
                                removeViewAt(currentIndex)
                                addView(activeChild.view, index)
                            } // иначе view на своем месте
                        } else { // иначе добавляем новую view
                            addView(activeChild.view, index)
                        }
                    }
                }
            )
        }
        this.currentChildren = activeChildren
        this.currentStack = stack
        validateInactive(stack)
    }

    /**
     * Создаем активные экраны для нового стека.
     * Причем если view экрана уже есть, то она переиспользуется.
     *
     * Тут используется свойство overlay.
     * Если у экрана overlay == true, то он работает по принципу add.
     * Если у экрана overlay == false, то он работает по принципу replace.
     * @see UiParams.overlay
     *
     * В активные экраны попадает самый верхний экран из стека.
     * Если он или его параметры определяют overlay == true, то в активные элементы попадает и предыдущий экран.
     * Для предыдущего экрана работает та же логика, и так далее до конца стэка.
     *
     * @return
     * activeChildren - список всех view, которые должны, быть в StackHostView. Причем некоторые из них могут быть уже добавлены.
     * removedChildren - список всех view, которые должны быть удалены из StackHostView.
     */
    private fun<C : Any, T : ViewRender> createActiveChildren(
        hostViewLifecycle: Lifecycle,
        stack: ChildStack<C, T>,
    ): Pair<List<ActiveChild<C, T>>, Collection<ActiveChild<C, T>>> {
        val activeChildren = LinkedList<ActiveChild<C, T>>()
        val currentMap = currentChildren.associateByTo(LinkedHashMap(), ActiveChild<*,*>::id) { it as ActiveChild<C, T> }
        for (i in stack.items.indices.reversed()) {
            val item = stack.items[i]
            val child = currentMap.remove(item.id()) ?: createActiveChild(hostViewLifecycle, item)
            activeChildren.addFirst(child) // поскольку итерируемся с конца, то добавляем в начало
            if (!child.overlay) break
        }
        val removedChildren = currentMap.values
        return Pair(activeChildren, removedChildren)
    }

    /**
     * Сохраняем состояния view, которые остались stack, но удалены из HostView.
     * Поскольку к ним можно вернуться.
     * В таком случае состояние будет восстановлено.
     *
     * @param removed Cписок удаляемых View.
     * @param stack Новый stack.
     */
    private fun<C : Any, T : ViewRender> saveRemovedChildren(
        removed: Collection<ActiveChild<C, T>>,
        stack: ChildStack<C, T>,
    ) {
        // Параметры, которые находятся в stack.
        val backStack = stack.backStack.asSequence().map { it.configuration }.toSet()
        // Если текущий экран остался в stack сохраняем его состояние.
        removed.asSequence()
            .filter { backStack.contains(it.child.configuration) }
            .forEach {
                saveActive(it)
            }
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
        // Сохраняем в inactiveChildren только те, которые находятся в новом stack.
        validateInactive(stack.items)
    }
}
