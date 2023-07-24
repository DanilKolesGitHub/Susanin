package com.example.navigation.navigation

import com.arkivanov.decompose.router.children.NavState
import com.example.navigation.router.NavigationDispatcher

internal class NavigationState(
    private val root: NavigationManager,
    private val navigationDispatcher: NavigationDispatcher,
) {
//    fun open(target: List<ScreenParams>) {
//        var node: NavigationNode = root
//        var lastNavigator: Navigator<*>? = null
//        var lastChild: ScreenParams? = null
//        target.subList(1, target.size).forEach { childParams ->
//            val childNode = node.findChild(childParams) ?: node.provideChild(childParams)
//            val navTag = navigationDispatcher.findRegisteredTag(node.params.key, childParams.key)
//            val holder = node.findHolder(navTag)
//            if (holder != null) {
//                lastNavigator = holder.navigator
//                lastChild = childParams
//            } else {
//                node.addInitialHolder(navTag, childParams)
//            }
//            node = childNode
//        }
//        lastNavigator!!.open(lastChild!!)
//    }
}

class NavigationManager(
    val params: Any,
    val parent: NavigationManager? = null
) {

    val children = HashMap<Any, NavigationManager>()

    internal fun <P: Any> provideChild(childParams: P): NavigationManager {
        return children.getOrPut(childParams) { NavigationManager(childParams, this) }
    }

    internal fun <P: Any> removeChild(childParams: P) {
        val child = children.remove(childParams)
        child?.clear()
    }

    internal fun <P: Any> findChild(childParams: P): NavigationManager? {
        return children[childParams]
    }

    private val navigatorHolders = HashMap<String, Holder>()

    internal fun <P: Any, S : NavState<P>> provideHolder(
        tag: String,
        factory: (initial: List<P>?) -> NavigationHolder<P, S>
    ) : NavigationHolder<P, S> {
        var holder = navigatorHolders.get(tag)
        if (holder == null) {
            holder = factory(null)
            navigatorHolders[tag] = holder
        }
        return holder as NavigationHolder<P, S>
    }

    internal fun <P: Any> findHolder(tag: String): NavigationHolder<P, *>? {
        return navigatorHolders[tag] as? NavigationHolder<P, *>
    }

    private fun clear(){
        children.clear()
        navigatorHolders.clear()
    }

    companion object {
        fun NavigationManager.dumpTree(): String{
            var root: NavigationManager = this
            while (root.parent != null)
                root = root.parent!!
            val builder = StringBuilder()
            root.dumpNode("", builder)
            return builder.toString()
        }

        fun NavigationManager.dumpNode(prefix: String, builder: StringBuilder) {
            builder.append(prefix).append(params.toString()).append("\n")
            val childPrefix = "$prefix    "
            children.values.forEach {
                it.dumpNode(childPrefix, builder)
            }
        }
    }
}

internal open class Holder(val tag: String)

internal open class NavigationHolder<P : Any, S: NavState<P>>(
    tag: String,
    val navigation: Navigation<P, S>,
    var state: S,
) : Holder(tag)