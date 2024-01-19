package com.example.navigation.stack

import android.animation.Animator
import android.animation.AnimatorSet
import android.content.Context
import android.util.AttributeSet
import com.arkivanov.decompose.Child
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.ObserveLifecycleMode
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.observe
import com.arkivanov.essenty.lifecycle.*
import com.example.navigation.view.*
import java.util.LinkedList

class StackHostView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : HostView(context, attrs, defStyleAttr) {

    private var currentStack: ChildStack<*, *>? = null

    /**
     * Активные экраны/view, которые добавлены в StackHostView.
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

        val (activeChildren, insertedChildren, removedChildren) = createActiveChildren(hostViewLifecycle, stack)
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
            removedChildren.forEach {
                // Если текущий экран остался в stack сохраняем его состояние.
                if (isInBackStack(stack, it)) {
                    saveActive(it)
                }
            }
            // Новый экран был в стеке, поэтому проигрываем анимацию текущего в обратную сторону.
            val activeFromStack = isInBackStack(currentStack, activeChild)
            // Во время анимации все view в состоянии STARTED.
            // По окончании анимации верхняя RESUMED, а удаленные DESTROYED.
            beginTransition(
                addToBack = activeFromStack,
                addChildren = activeChildren,
                removeChildren = removedChildren,
                animatorProvider = { provideTransition(currentChild, activeChild, removedChildren, insertedChildren, activeFromStack) },
                onStart = {
                    removedChildren.map(ActiveChild<*,*>::lifecycle).forEach(LifecycleRegistry::pause)
                    currentChild.lifecycle.pause()
                    activeChildren.map(ActiveChild<*,*>::lifecycle).forEach(LifecycleRegistry::start)
                },
                onEnd = {
                    removedChildren.map(ActiveChild<*,*>::lifecycle).forEach(LifecycleRegistry::destroy)
                    activeChild.lifecycle.resume()
                },
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
     * insertedChildren - список всех view, которые были созданы.
     * removedChildren - список всех view, которые должны быть удалены из StackHostView.
     */
    private fun<C : Any, T : ViewRender> createActiveChildren(
        hostViewLifecycle: Lifecycle,
        stack: ChildStack<C, T>,
    ): Triple<List<ActiveChild<C, T>>, Collection<ActiveChild<C, T>>, Collection<ActiveChild<C, T>>> {
        val activeChildren = LinkedList<ActiveChild<C, T>>()
        val insertedChildren = LinkedList<ActiveChild<C, T>>()
        val currentMap = currentChildren.associateByTo(LinkedHashMap(), ActiveChild<*,*>::id) { it as ActiveChild<C, T> }
        val overlayed = LinkedList<Child.Created<C, T>>()
        for (i in stack.items.indices.reversed()) {
            val item = stack.items[i]
            overlayed.addFirst(item) // поскольку итерируемся с конца, то добавляем в начало
            if (!item.overlay) break
        }
        overlayed.forEach { item ->
            val child = currentMap.remove(item.id()) ?:
                    createActiveChild(hostViewLifecycle, item).also(insertedChildren::add)
            activeChildren.add(child)
        }
        val removedChildren = currentMap.values
        return Triple(activeChildren, insertedChildren, removedChildren)
    }

    /**
     * Предоставляет анимацию.
     * Если предыдущего экрана не было, то не анимируем появление первого экрана.
     * Если новый эран из стека, то проигрываем анимацию удаления текущего.
     * Иначе проигрываем анимацию добавления нового.
     *
     * @param current Текущий экран.
     * @param active Новый экран.
     * @param removedChildren Удаляемые экраны.
     * @param insertedChildren Добавленные экраны.
     * @param back Возврат назад по стеку.
     */
    private fun provideTransition(
        current: ActiveChild<*, *>,
        active: ActiveChild<*, *>,
        removedChildren: Collection<ActiveChild<*, *>>,
        insertedChildren: Collection<ActiveChild<*, *>>,
        back: Boolean
    ): Animator? {
        val transition = if (back) current.viewTransition else active.viewTransition
        transition ?: return null
        val animations = LinkedList<Animator?>()
        if (!back) {
            animations += transition.enterThis(active.view, this)
            insertedChildren.asSequence()
                .filter { it.id != current.id && it.id != active.id }
                .forEach { animations += transition.enterThis(it.view, this) }
            animations += transition.exitOther(current.view, this)
            removedChildren.asSequence()
                .filter { it.id != current.id && it.id != active.id }
                .forEach { animations += transition.exitOther(it.view, this) }
        } else {
            animations += transition.enterOther(active.view, this)
            insertedChildren.asSequence()
                .filter { it.id != current.id && it.id != active.id }
                .forEach { animations += transition.enterOther(it.view, this) }
            animations += transition.exitThis(current.view, this)
            removedChildren.asSequence()
                .filter { it.id != current.id && it.id != active.id }
                .forEach { animations += transition.exitThis(it.view, this) }
        }
        return animations.filterNotNull().run {
            if (isNotEmpty()) {
                AnimatorSet().also { it.playTogether(this) }
            } else {
                null
            }
        }
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
