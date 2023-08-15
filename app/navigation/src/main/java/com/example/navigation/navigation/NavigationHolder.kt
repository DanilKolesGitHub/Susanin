package com.example.navigation.navigation

import com.arkivanov.decompose.router.children.NavState

/**
 * Класс для хранения navigation.
 * Используется для навигации к дочерним компонентам.
 * Различные реализации этого класса определяют различное поведение.
 * Смотри SlotNavigationHolder, StackNavigationHolder.
 */
internal open class NavigationHolder<P : Any, S : NavState<P>>(
    val tag: String,
    val navigation: Navigation<P, S>,
    var state: S,
)
