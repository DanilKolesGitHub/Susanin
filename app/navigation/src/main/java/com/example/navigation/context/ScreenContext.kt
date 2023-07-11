package com.example.navigation.context

import com.arkivanov.decompose.ComponentContext
import com.example.navigation.navigation.NavigationNode

interface ScreenContext : ComponentContext {
    val node: NavigationNode
}

class DefaultScreenContext (
    componentContext: ComponentContext,
    override val node: NavigationNode,
): ScreenContext, ComponentContext by componentContext