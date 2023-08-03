package com.example.navigation.context

import com.arkivanov.decompose.ComponentContext
import com.example.navigation.navigation.NavigationManager
import com.example.navigation.router.Router
import com.example.navigation.screens.ScreenParams


interface NavigationContext<P : Any> : ComponentContext {
    val navigation: NavigationManager<P>
}

class DefaultNavigationContext<P : Any> (
    componentContext: ComponentContext,
    override val navigation: NavigationManager<P>,
): NavigationContext<P>, ComponentContext by componentContext

interface ScreenContext : NavigationContext<ScreenParams>{
    val router: Router
}

class DefaultScreenContext (
    navigationContext: NavigationContext<ScreenParams>,
    override val router: Router,
): ScreenContext, NavigationContext<ScreenParams> by navigationContext