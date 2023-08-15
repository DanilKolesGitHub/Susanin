package com.example.navigation.dialogs

import com.arkivanov.decompose.Child

data class ChildDialogs<out C : Any, out T : Any>(
    val dialogs: List<Child.Created<C, T>> = emptyList(),
) {
    val active: Child.Created<C, T>?
        get() = dialogs.lastOrNull()
}

