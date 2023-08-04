package com.example.navigation.navigation

import com.example.navigation.transaction.Path
import com.example.navigation.tree.Tree
import kotlin.reflect.KClass

typealias Type<P> = KClass<out P>
class NavigationDispatcher<P : Any>(
    private val registeredHost: Map<Type<P>, Map<String, Set<Type<P>>>>,
    private val default: Map<Type<P>, P>,
) {

    internal val tree: Tree<Type<P>>

    init {
        val dependencyMap =
            registeredHost.mapValues { host -> host.value.flatMapTo(hashSetOf()) { it.value } }
        tree = Tree(dependencyMap)
    }

    internal fun preparePath(path: Path<P>, from: List<P>): List<Type<P>> {
        val fromTypes: List<Type<P>> = from.map { it::class }
        val pathTypes = path.types
        return tree.findPath(fromTypes, pathTypes)
    }

    internal fun defaultParam(type: Type<P>): P? {
        return default[type]
    }

    internal fun findTagForChild(host: Type<P>, child: Type<P>): String {
        val registeredScreensInHost = registeredHost[host]
            ?: throw IllegalStateException("Host $host has not registeredHost screen $child")
        registeredScreensInHost.forEach { (tag, screens) ->
            if (screens.contains(child))
                return tag
        }
        throw IllegalStateException("Host $host has not registeredHost screen $child")
    }
}
