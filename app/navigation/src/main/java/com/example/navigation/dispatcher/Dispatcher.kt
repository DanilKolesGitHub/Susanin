package com.example.navigation.dispatcher

import com.example.navigation.navigation.NavigationState
import com.example.navigation.screens.ScreenKey
import com.example.navigation.screens.ScreenParams
import com.example.navigation.tree.Tree
import kotlin.reflect.KClass

internal class Dispatcher(
    private val tree: Tree<ScreenKey>,
    private val defaultParams: Map<ScreenKey, ScreenParams>,
    private val navigationState: NavigationState,
) {
    fun open(path: List<Any>, from: List<ScreenParams>) {
        navigationState.open(preparePath(path, from))
    }

    private fun preparePath(screens: List<Any>, from: List<ScreenParams>): List<ScreenParams> {
        val active = from.map(ScreenParams::key)
        val target = getTargetScreens(screens)
        val path = tree.findPath(active, target)
        return getPathScreenParams(path)
    }

    private fun getTargetScreens(screens: List<Any>): List<ScreenKey> {
        val list = mutableListOf<ScreenKey>()
        screens.forEach {
            when {
                it is ScreenParams -> list.add(it.key)
                it is KClass<*> && it as? ScreenKey != null -> list.add(it)
                else -> throw IllegalArgumentException("Unsupported type $it")
            }
        }
        return list
    }

    private fun getPathScreenParams(path: List<ScreenKey>): List<ScreenParams> {
        val list = mutableListOf<ScreenParams>()
        path.forEach {
            when {
                it is ScreenParams -> list.add(it)
                defaultParams.containsKey(it) -> list.add(defaultParams[it]!!)
                else -> throw IllegalArgumentException("No default ScreenParams for $it")
            }
        }
        return list
    }
}