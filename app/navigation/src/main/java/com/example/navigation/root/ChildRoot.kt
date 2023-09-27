package com.example.navigation.root

import com.arkivanov.decompose.Child

data class ChildRoot<out C : Any, out T : Any>(
    val child: Child.Created<C, T>,
)