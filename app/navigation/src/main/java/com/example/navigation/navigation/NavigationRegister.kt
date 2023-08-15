package com.example.navigation.navigation

import com.example.navigation.NavigationType
import com.example.navigation.dispatcher.Type

class NavigationRegister<P : Any> {

    val navigation: MutableMap<Type<P>, MutableMap<String, MutableSet<Type<P>>>> = mutableMapOf()
    val default: MutableMap<Type<P>, P> = HashMap()

    fun registerDefault(params: P) {
        if (default.containsKey(params::class)) {
            return
        }
        default[params::class] = params
    }

    fun registerNavigation(host: Type<P>, tag: String, vararg params: Type<P>) {
        navigation
            .getOrPut(host) { HashMap(1) }
            .getOrPut(tag) { HashSet(params.size) }
            .addAll(params)
    }

    fun registerStackNavigation(host: Type<P>, vararg params: Type<P>) {
        registerNavigation(host, NavigationType.STACK.name, *params)
    }

    fun registerSlotNavigation(host: Type<P>, vararg params: Type<P>) {
        registerNavigation(host, NavigationType.SLOT.name, *params)
    }

    fun registerPagesNavigation(host: Type<P>, vararg params: Type<P>) {
        registerNavigation(host, NavigationType.PAGES.name, *params)
    }
}

