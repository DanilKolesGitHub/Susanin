package com.example.navigation.navigation

import com.arkivanov.decompose.router.children.NavState
import com.arkivanov.decompose.router.children.NavigationSource
import com.arkivanov.decompose.router.children.SimpleNavigation

interface Navigation<P : Any,  S : NavState<P>> : NavigationSource<Event<S>>, Navigator<P, S>

class Event<S : NavState<Any>>(
    val transformer: (state: S) -> S,
    val onComplete: (newState: S, oldState: S) -> Unit = { _, _ -> },
)

interface Navigator<P : Any,  S : NavState<P>>  {

    fun open(
        params: P,
        onComplete: (newState: S, oldState: S) -> Unit = { _, _ -> },
    )

    fun close(
        params: P,
        onComplete: (newState: S, oldState: S) -> Unit = { _, _ -> },
    )

    fun canBack(state: S): Boolean

    fun back(state: S): S

}

abstract class DefaultNavigation<P : Any, S : NavState<P>> : Navigation<P, S> {

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