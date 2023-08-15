package com.example.navigation.navigation

import com.arkivanov.decompose.router.children.NavState
import com.example.navigation.dispatcher.NavigationDispatcher
import com.example.navigation.dispatcher.Type
import java.util.LinkedList

/**
 * Ключевой класс для работы с навигацией.
 * Создается для каждого NavigationContext.
 * Имеет древовидную структуру, которая полностью повторяет структуру компонентов.
 *
 * @param params
 * Для каждого NavigationManager заданы уникальные params. Используется как id компонента.
 * @param parent
 * Родительский NavigationManager.
 * Дочерний компонент не может существовать долше родительского, поэтому передается в конструктор.
 * Может быть null для корневого компонента.
 *
 */
class NavigationManager<P : Any>(
    val params: P,
    val parent: NavigationManager<P>? = null,
    val dispatcher: NavigationDispatcher<P>
) {
    /**
     * Список дочерних мэнеджеров.
     */
    private val children = HashMap<P, NavigationManager<P>>()

    /**
     * Список NavigationHolders, с помощью которых происходит навигация внутри компонента.
     * Если в экране не открываются другие экраны, то список пуст.
     */
    private val navigationHolders = HashMap<String, NavigationHolder<P, *>>()

    /**
     * Отложенные параметры.
     * Заполняются, когда нужно открыть дочерний компонент, но компонент еще не создан.
     */
    private val pending = HashMap<String, List<P>>()

    /**
     * Предоставляет все параметры от корня до текущего NavigationManager.
     */
    internal val branch: LinkedList<P>
        get() {
            return (parent?.branch ?: LinkedList<P>()).apply { addLast(params) }
        }

    /**
     * Ищет корневой NavigationManager.
     */
    internal val root: NavigationManager<P>
        get() {
            var root = this
            while (root.parent != null) {
                root = root.parent!!
            }
            return root
        }

    /**
     * Предоставляет дочерний NavigationManager или создает его, если его нет.
     */
    internal fun provideChild(childParams: P): NavigationManager<P> {
        return children.getOrPut(childParams) { NavigationManager(childParams, this, dispatcher) }
    }

    /**
     * Предоставляет дочерний NavigationManager или null, если его нет.
     */
    internal fun findChild(childParams: P): NavigationManager<P>? {
        return children[childParams]
    }

    /**
     * Предоставляет параметры по типу если они уже находятся в дочерних компонентах или null, если нет.
     */
    internal fun findChildParams(type: Type<P>): P? {
        return children.keys.find { it::class == type }
    }

    /**
     * Добавляет параметры в отложенные.
     */
    internal fun pendingParams(tag: String, vararg pendingParams: P) {
        pending[tag] = pendingParams.toList()
    }

    /**
     * Удаляет дочерний NavigationManager.
     */
    internal fun removeChild(childParams: P) {
        children.remove(childParams)?.clear()
    }

    /**
     * Предоставляет NavigationHolder или создает его, если его нет.
     * @param tag Уникльный для каждого NavigationHolder идентификатор.
     * @param factory Фабрика для создания NavigationHolder.
     * На вход передаются отложенный параметры, которые нужно установить как initialState.
     */
    internal fun <S : NavState<P>> provideHolder(
        tag: String,
        factory: (initial: List<P>?) -> NavigationHolder<P, S>,
    ): NavigationHolder<P, S> {
        return navigationHolders.getOrPut(tag) {
            factory(pending.remove(tag))
        } as NavigationHolder<P, S>
    }

    /**
     * Предоставляет дочерний NavigationHolder или null, если его нет.
     * @param tag Уникльный для каждого NavigationHolder идентификатор.
     */
    internal fun findHolder(tag: String): NavigationHolder<P, *>? {
        return navigationHolders[tag]
    }

    private fun clear() {
        children.clear()
        navigationHolders.clear()
        pending.clear()
    }

    private companion object {
        // Для дебага можно посмотреть иерархию навигаторов
        fun <P : Any> NavigationManager<P>.dumpTree(): String {
            var root: NavigationManager<P> = this
            while (root.parent != null)
                root = root.parent!!
            val builder = StringBuilder()
            root.dumpNode("", builder)
            return builder.toString()
        }

        fun <P : Any> NavigationManager<P>.dumpNode(prefix: String, builder: StringBuilder) {
            builder.append(prefix).append(params.toString()).append("\n")
            val childPrefix = "$prefix    "
            children.values.forEach {
                it.dumpNode(childPrefix, builder)
            }
        }
    }
}
