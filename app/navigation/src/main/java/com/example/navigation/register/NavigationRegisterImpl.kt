package com.example.navigation.register

import com.example.navigation.dispatcher.NavigationDispatcher
import com.example.navigation.dispatcher.Type

class NavigationRegisterImpl<P : Any> : NavigationRegister<P> {

    private val navigationMap = HashMap<Type<P>, HashMap<String, HashSet<Type<P>>>>()
    private val default = HashMap<Type<P>, P>()

    @Synchronized
    override fun registerNavigation(type: Type<P>, hierarchyMap: Map<String, Set<Type<P>>>) {
        val registeredHierarchyMap = navigationMap.getOrPut(type) { HashMap() }
        hierarchyMap.forEach { (tag, children) ->
            registeredHierarchyMap
                .getOrPut(tag) { HashSet() }
                .addAll(children)
        }
    }

    @Synchronized
    override fun registerNavigation(params: P, hierarchyMap: Map<String, Set<Type<P>>>) {
        val type = params::class
        default[type] = params
        registerNavigation(type, hierarchyMap)
    }

    override fun dispatcher() =
        NavigationDispatcher(
            navigationMap,
            default,
        )
}
