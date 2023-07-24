package com.example.navigation.context

import com.arkivanov.decompose.ComponentContext
import com.example.navigation.navigation.NavigationManager
import com.example.navigation.router.Router


interface NavigationContext : ComponentContext {
    val navigation: NavigationManager
}

class DefaultNavigationContext (
    componentContext: ComponentContext,
    override val navigation: NavigationManager,
): NavigationContext, ComponentContext by componentContext

interface ScreenContext : NavigationContext{
    val router: Router
}

class DefaultScreenContext (
    navigationContext: NavigationContext,
    override val router: Router,
): ScreenContext, NavigationContext by navigationContext