package com.example.navigation.pages

import com.example.navigation.navigation.NavigationHolder
import com.example.navigation.screens.ScreenParams
import com.example.navigation.slot.SlotHostState

internal class PagesNavigationHolder(
    tag: String,
    pages: List<ScreenParams>,
    selected: Int,
    closeBehaviour: PagesNavigation.CloseBehaviour,
    backBehaviour: PagesNavigation.BackBehaviour,
): NavigationHolder<PagesHostState>(
    tag,
    PagesNavigation(closeBehaviour, backBehaviour),
    PagesHostState(pages, selected)
)