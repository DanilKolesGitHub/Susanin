package com.example.navigation.state

import com.arkivanov.decompose.router.children.ChildNavState
import com.example.navigation.screens.ScreenParams

data class ChildState(
    override val configuration: ScreenParams,
    override val status: ChildNavState.Status
) : ChildNavState<ScreenParams>