package com.example.navigation.context

import com.arkivanov.decompose.ComponentContext
import com.example.navigation.navigation.NavigationNode
import com.example.navigation.router.Router


interface NavigationContext : ComponentContext {
    val navigation: NavigationNode
}

class DefaultNavigationContext (
    componentContext: ComponentContext,
    override val navigation: NavigationNode,
): NavigationContext, ComponentContext by componentContext

interface ScreenContext : NavigationContext{
    val router: Router
}

class DefaultScreenContext (
    navigationContext: NavigationContext,
    override val router: Router,
): ScreenContext, NavigationContext by navigationContext