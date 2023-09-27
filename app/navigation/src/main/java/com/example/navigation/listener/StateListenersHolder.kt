package com.example.navigation.listener

internal class StateListenersHolder {

    private val listeners =  HashMap<String, LinkedHashSet<StateListener<*>>>()

    fun <S> add(tag: String, listener: StateListener<S>) {
        listeners.getOrPut(tag) { LinkedHashSet() }.add(listener)
    }

    fun <S> remove(tag: String, listener: StateListener<S>) {
        listeners[tag]?.remove(listener)
        if (listeners[tag].isNullOrEmpty())
            listeners.remove(tag)
    }

    fun <S> onBeforeApply(tag: String, newState: S, oldState: S) {
        listeners[tag]?.forEach {
            (it as StateListener<S>).onBeforeApply(newState, oldState)
        }
    }

    fun <S> onAfterApply(tag: String, newState: S, oldState: S) {
        listeners[tag]?.forEach {
            (it as StateListener<S>).onAfterApply(newState, oldState)
        }
    }
}