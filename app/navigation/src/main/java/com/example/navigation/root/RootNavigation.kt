package com.example.navigation.root

import com.arkivanov.decompose.router.children.NavState
import com.arkivanov.decompose.router.children.NavigationSource
import com.arkivanov.decompose.router.children.SimpleNavigation

class RootNavigation : NavigationSource<Event<RootHostState>>{

    private val relay = SimpleNavigation<Event<RootHostState>>()

    override fun subscribe(observer: (Event<RootHostState>) -> Unit) {
        relay.subscribe(observer)
    }

    override fun unsubscribe(observer: (Event<RootHostState>) -> Unit) {
        relay.unsubscribe(observer)
    }

    fun initialized() {
        relay.navigate(Event { it.copy(initialized = true)})
    }
}

class Event<S : NavState<Any>>(val transformer: (state: S) -> S)