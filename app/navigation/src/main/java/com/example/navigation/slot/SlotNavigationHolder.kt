package com.example.navigation.slot

import com.example.navigation.navigation.NavigationHolder
import com.example.navigation.screens.ScreenParams

internal class SlotNavigationHolder(
    tag: String,
    initial: ScreenParams?
): NavigationHolder<SlotHostState>(
    tag,
    SlotNavigation(),
    SlotHostState(initial)
)