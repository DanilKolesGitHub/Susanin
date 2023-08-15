package com.example.navigation.context

import com.arkivanov.decompose.ComponentContext
import com.example.navigation.navigation.NavigationManager
import com.example.navigation.register.NavigationRegister

interface NavigationContext<P : Any> : ComponentContext {
    val navigation: NavigationManager<P>
}

class DefaultNavigationContext<P : Any>(
    componentContext: ComponentContext,
    override val navigation: NavigationManager<P>,
) : NavigationContext<P>, ComponentContext by componentContext

fun <P : Any> NavigationRegister<P>.defaultNavigationContext(
    componentContext: ComponentContext,
    rootParams: P,
): NavigationContext<P> {
    return DefaultNavigationContext(
        componentContext,
        NavigationManager(
            rootParams,
            null,
            dispatcher(),
        ),
    )
}
