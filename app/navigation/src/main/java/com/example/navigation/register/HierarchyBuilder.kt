package com.example.navigation.register

import com.example.navigation.dispatcher.Type

class HierarchyBuilder<P : Any> {

    internal val hierarchyMap = HashMap<String, HashSet<Type<P>>>()

    fun navigation(tag: String, vararg types: Type<P>) {
        hierarchyMap.getOrPut(tag) { HashSet() }.addAll(types)
    }
}
