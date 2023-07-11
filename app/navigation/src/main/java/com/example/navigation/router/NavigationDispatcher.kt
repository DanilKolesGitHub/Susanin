package com.example.navigation.router

import com.example.navigation.screens.ScreenKey
import com.example.navigation.screens.ScreenParams
import com.example.navigation.tree.Tree

internal class NavigationDispatcher(
    private val registeredScreens: Map<ScreenKey, Map<String, Set<ScreenKey>>>,
    private val defaultParams: Map<ScreenKey, ScreenParams>,
) {
    private val tree: Tree<ScreenKey>

    init {
        val dependencyMap = registeredScreens.mapValues { it.value.flatMapTo(hashSetOf()) { it.value } }
        tree = Tree(dependencyMap)
    }

    fun findRegisteredTag(host: ScreenKey, target: ScreenKey): String {
        val registeredScreensInHost = registeredScreens[host] ?: throw IllegalStateException("Host $host has not registered screen $target")
        registeredScreensInHost.forEach { (tag, screens) ->
            if(screens.contains(target))
                return tag
        }
        throw IllegalStateException("Host $host has not registered screen $target")
    }

    fun findRegisteredScreens(host: ScreenKey, tag: String): Set<ScreenKey> {
        val registeredScreensInHost = registeredScreens[host] ?: throw IllegalStateException("Host $host has not registered screen")
        return registeredScreensInHost[tag] ?: throw IllegalStateException("Host $host has not registered screen for $tag")
    }
}