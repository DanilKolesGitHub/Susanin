package com.example.navigation.navigation

import com.arkivanov.decompose.router.children.NavState
import com.example.navigation.router.NavigationDispatcher

internal class NavigationState(
    private val root: NavigationNode,
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

class NavigationNode(
    val params: Any,
    val parent: NavigationNode? = null
) {

    val children = HashMap<Any, NavigationNode>()

    fun <P: Any> provideChild(childParams: P): NavigationNode {
        return children.getOrPut(childParams) { NavigationNode(childParams, this) }
    }

    fun <P: Any> removeChild(childParams: P) {
        children.remove(childParams)
    }

    fun <P: Any> findChild(childParams: P): NavigationNode? {
        return children[childParams]
    }

    private val navigators = HashMap<String, Holder>()

    fun <P: Any, S : NavState<P>> provideNavigation(
        tag: String,
        factory: (initial: List<P>?) -> NavigationHolder<P, S>
    ) : NavigationHolder<P, S> {
        var holder = navigators.get(tag)
        if (holder == null) {
            holder = factory(null)
            navigators[tag] = holder
        }
        return holder as NavigationHolder<P, S>
    }

    fun <P: Any> findHolder(tag: String): NavigationHolder<P, *>? {
        return navigators[tag] as? NavigationHolder<P, *>
    }
}

open class Holder(val tag: String)

open class NavigationHolder<P : Any, S: NavState<P>>(
    tag: String,
    val navigator: Navigation<P, S>,
    var state: S,
) : Holder(tag)