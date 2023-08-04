package com.example.navigation.navigation

import com.arkivanov.decompose.router.children.NavState
import com.example.navigation.transaction.Path
import java.util.LinkedList

class NavigationManager<P : Any>(
    private val params: P,
    private val parent: NavigationManager<P>? = null,
    private val dispatcher: NavigationDispatcher<P>,
) {

    val children = HashMap<P, NavigationManager<P>>()

    val tree = dispatcher.tree

    internal fun provideChild(childParams: P): NavigationManager<P> {
        return children.getOrPut(childParams) { NavigationManager(childParams, this, dispatcher) }
    }

    internal fun findChild(childParams: P): NavigationManager<P>? {
        return children.get(childParams)
    }

    internal fun removeChild(childParams: P) {
        val child = children.remove(childParams)
        child?.clear()
    }

    /**
     * Предоставляет все параметры от корня до текущего NavigationManager
     */
    private val branch: LinkedList<P>
        get() {
            return (parent?.branch ?: LinkedList<P>()).apply { addLast(params) }
        }

    /**
     * Ищет корневой NavigationManager
     */
    private val root: NavigationManager<P>
        get() {
            var root = this
            while (root.parent != null) {
                root = root.parent!!
            }
            return root
        }

    private val navigatorHolders = HashMap<String, Holder>()

    internal fun <S : NavState<P>> provideHolder(
        tag: String,
        factory: (pending: P?) -> NavigationHolder<P, S>
    ): NavigationHolder<P, S> {
        var holder = navigatorHolders.get(tag)
        if (holder == null || holder is PendingHolder<*>) {
            holder = factory((holder as? PendingHolder<P>)?.pending)
            navigatorHolders[tag] = holder
        }
        return holder as NavigationHolder<P, S>
    }

    private fun findHolder(tag: String): NavigationHolder<P, *>? {
        return navigatorHolders[tag] as? NavigationHolder<P, *>
    }

    private fun pendingHolder(tag: String, pending: P) {
        navigatorHolders[tag] = PendingHolder(tag, pending)
    }

    private fun clear() {
        children.clear()
        navigatorHolders.clear()
    }

    internal fun open(path: Path<P>) {
        val fullPath = dispatcher.preparePath(path, branch)
        var current = root
        val pathParams = path.params
        val operations = LinkedList<() -> Unit>()
        fullPath.forEachIndexed { index, currentType ->
            if (index >= fullPath.size - 1) return@forEachIndexed
            if (current.params::class != currentType) throw IllegalStateException("Illegal type $currentType for current ${current.params}")
            val childType = fullPath[index + 1]
            val childParam = pathParams[childType] ?:
                            current.childParam(childType) ?:
                            dispatcher.defaultParam(childType) ?:
                            throw IllegalStateException("Can not find params fot type $childType")
            val child = current.provideChild(childParam)

            val tag = dispatcher.findTagForChild(currentType, childType)
            val navigationHolder = current.findHolder(tag)
            if (navigationHolder != null) {
                operations.add { navigationHolder.navigation.open(childParam) }
            } else {
                current.pendingHolder(tag, childParam)
            }
            current = child
        }
        operations.forEach { it.invoke() }
    }

    internal fun close(path: Path<P>) {
        val fullPath = dispatcher.preparePath(path, branch)
        var current = root
        val pathParams = path.params
        var operation: () -> Unit = {}
        fullPath.forEachIndexed { index, currentType ->
            if (index >= fullPath.size - 1) return@forEachIndexed
            if (current.params::class != currentType) throw IllegalStateException("Illegal type $currentType for current ${current.params}")
            val childType = fullPath[index + 1]
            val childParam = pathParams[childType] ?:
                            current.childParam(childType) ?:
                            dispatcher.defaultParam(childType) ?:
                            throw IllegalStateException("Can not find params fot type $childType")
            val child = current.findChild(childParam) ?: return

            val tag = dispatcher.findTagForChild(currentType, childType)
            val navigationHolder = current.findHolder(tag) ?: return
            if (index == fullPath.size - 2) {
                operation = { navigationHolder.navigation.close(childParam) }
            }
            current = child
        }
        operation.invoke()
    }

    private fun childParam(type: Type<P>): P? {
        return children.keys.find { it::class == type }
    }

    companion object {

        fun NavigationManager<*>.dumpTree(): String {
            var root: NavigationManager<*> = this
            while (root.parent != null)
                root = root.parent!!
            val builder = StringBuilder()
            root.dumpNode("", builder)
            return builder.toString()
        }

        fun NavigationManager<*>.dumpNode(prefix: String, builder: StringBuilder) {
            builder.append(prefix).append(params.toString()).append("\n")
            val childPrefix = "$prefix    "
            children.values.forEach {
                it.dumpNode(childPrefix, builder)
            }
        }
    }
}

internal open class Holder(val tag: String)

internal open class PendingHolder<P : Any>(
    tag: String,
    val pending: P,
) : Holder(tag)

internal open class NavigationHolder<P : Any, S : NavState<P>>(
    tag: String,
    val navigation: Navigation<P, S>,
    var state: S,
) : Holder(tag)