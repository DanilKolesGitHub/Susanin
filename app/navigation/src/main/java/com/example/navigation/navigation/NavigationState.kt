package com.example.navigation.navigation

import com.example.navigation.router.NavigationDispatcher
import com.example.navigation.router.Router
import com.example.navigation.screens.ScreenParams
import com.example.navigation.state.HostState

internal class NavigationState(
    private val root: NavigationNode,
    private val navigationDispatcher: NavigationDispatcher,
) {
    fun open(target: List<ScreenParams>) {
        var node: NavigationNode = root
        var lastNavigator: Navigator<*>? = null
        var lastChild: ScreenParams? = null
        target.subList(1, target.size).forEach { childParams ->
            val childNode = node.findChild(childParams) ?: node.provideChild(childParams)
            val navTag = navigationDispatcher.findRegisteredTag(node.params.key, childParams.key)
            val holder = node.findHolder(navTag)
            if (holder != null) {
                lastNavigator = holder.navigator
                lastChild = childParams
            } else {
                node.addInitialHolder(navTag, childParams)
            }
            node = childNode
        }
        lastNavigator!!.open(lastChild!!)
    }
}

class NavigationNode(
    val params: ScreenParams,
    val parent: NavigationNode? = null,
    val router: Router,
) {

    val children = HashMap<ScreenParams, NavigationNode>()

    fun provideChild(childParams: ScreenParams): NavigationNode {
        return children.getOrPut(childParams) { NavigationNode(childParams, this, router) }
    }

    fun removeChild(childParams: ScreenParams) {
        children.remove(childParams)
    }

    fun findChild(childParams: ScreenParams): NavigationNode? {
        return children[childParams]
    }

    private val navigators = HashMap<String, Holder>()

    fun <S : HostState> provideNavigation(
        tag: String,
        factory: (initial: List<ScreenParams>?) -> NavigationHolder<S>
    ) : NavigationHolder<S> {
        var holder = navigators.get(tag)
        if (holder == null) {
            holder = factory(null)
            navigators[tag] = holder
        } else if (holder is InitialHolder) {
            holder = factory(holder.initial)
            navigators[tag] = holder
        }
        return holder as NavigationHolder<S>
    }

    fun findHolder(tag: String): NavigationHolder<*>? {
        return navigators[tag] as? NavigationHolder<*>
    }

    internal fun addInitialHolder(tag: String, params: ScreenParams) {
        navigators[tag] = InitialHolder(tag, listOf(params))
    }

}

open class Holder(val tag: String)

class InitialHolder(
    tag: String,
    val initial: List<ScreenParams>? = null
) : Holder(tag)

open class NavigationHolder<S : HostState>(
    tag: String,
    val navigator: Navigation<S>,
    var state: S,
) : Holder(tag)