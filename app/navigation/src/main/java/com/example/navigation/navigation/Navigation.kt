package com.example.navigation.navigation

import com.arkivanov.decompose.router.children.NavigationSource
import com.arkivanov.decompose.router.children.SimpleNavigation
import com.example.navigation.screens.ScreenParams
import com.example.navigation.state.HostState

interface Navigation<S : HostState> : NavigationSource<Event<S>>, Navigator<S>

class Event<S : HostState>(
    val transformer: (state: S) -> S,
    val onComplete: (newState: S, oldState: S) -> Unit = { _, _ -> },
)

interface Navigator<S: HostState>  {

    fun open(
        screenParams: ScreenParams,
        onComplete: (newState: S, oldState: S) -> Unit = { _, _ -> },
    )

    fun close(
        screenParams: ScreenParams,
        onComplete: (newState: S, oldState: S) -> Unit = { _, _ -> },
    )

    fun back(state: S): (() -> S)?

}

abstract class DefaultNavigation<S : HostState> : Navigation<S> {

    private val relay = SimpleNavigation<Event<S>>()

    protected fun navigate(transformer: (S) -> S, onComplete: (newState: S, oldState: S) -> Unit) {
        relay.navigate(Event(transformer, onComplete))
    }

    override fun subscribe(observer: (Event<S>) -> Unit) {
        relay.subscribe(observer)
    }

    override fun unsubscribe(observer: (Event<S>) -> Unit) {
        relay.unsubscribe(observer)
    }
}