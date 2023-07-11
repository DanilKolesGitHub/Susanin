package com.example.navigation.stack

import com.example.navigation.navigation.NavigationHolder
import com.example.navigation.screens.ScreenParams

internal class StackNavigationHolder(
    tag: String,
    initial: List<ScreenParams>
): NavigationHolder<StackHostState>(
    tag,
    StackNavigation(),
    StackHostState(initial)
)