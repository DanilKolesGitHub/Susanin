package com.example.navigation.register

import com.example.navigation.dispatcher.NavigationDispatcher
import com.example.navigation.dispatcher.Type

interface NavigationRegister<P : Any> {

    fun registerNavigation(default: P, builder: HierarchyBuilder<P>.() -> Unit) {
        val hierarchy = HierarchyBuilder<P>()
        hierarchy.builder()
        registerNavigation(default, hierarchy.hierarchyMap)
    }

    fun registerNavigation(type: Type<P>, builder: HierarchyBuilder<P>.() -> Unit) {
        val hierarchy = HierarchyBuilder<P>()
        hierarchy.builder()
        registerNavigation(type, hierarchy.hierarchyMap)
    }

    fun registerNavigation(type: Type<P>, hierarchyMap: Map<String, Set<Type<P>>>)

    fun registerNavigation(default: P, hierarchyMap: Map<String, Set<Type<P>>>)

    fun dispatcher(): NavigationDispatcher<P>
}
