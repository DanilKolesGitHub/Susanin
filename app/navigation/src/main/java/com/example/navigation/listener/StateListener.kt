package com.example.navigation.listener

internal interface StateListener<S> {

    fun onBeforeApply(newState: S, oldState: S?) = Unit

    fun onAfterApply(newState: S, oldState: S?) = Unit
}