package com.example.navigation.context

import com.arkivanov.decompose.ComponentContext
import com.example.navigation.register.ScreenRegister
import com.example.navigation.screens.ScreenParams

interface ScreenContext : NavigationContext<ScreenParams> {
    val extensions: ScreenExtensions
}

class DefaultScreenContext(
    navigationContext: NavigationContext<ScreenParams>,
    override val extensions: ScreenExtensions,
) : ScreenContext, NavigationContext<ScreenParams> by navigationContext

fun ScreenRegister.defaultScreenContext(
    componentContext: ComponentContext,
    rootParams: ScreenParams,
): ScreenContext {
    return DefaultScreenContext(
        defaultNavigationContext(
            componentContext,
            rootParams,
        ),
        screenExtensions(),
    )
}
