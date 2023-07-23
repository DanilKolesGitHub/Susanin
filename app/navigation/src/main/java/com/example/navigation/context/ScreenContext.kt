package com.example.navigation.context

import com.arkivanov.decompose.ComponentContext
import com.example.navigation.navigation.NavigationNode


interface NavigationContext : ComponentContext {
    val navigation: NavigationNode
}

class DefaultNavigationContext (
    componentContext: ComponentContext,
    override val navigation: NavigationNode,
): NavigationContext, ComponentContext by componentContext

interface ScreenContext : NavigationContext

class DefaultScreenContext (
    componentContext: ComponentContext,
    override val navigation: NavigationNode,
): NavigationContext by DefaultNavigationContext(componentContext, navigation)