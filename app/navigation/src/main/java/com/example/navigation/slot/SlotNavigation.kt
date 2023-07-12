package com.example.navigation.slot

import com.example.navigation.navigation.DefaultNavigation
import com.example.navigation.screens.ScreenParams

internal class SlotNavigation: DefaultNavigation<SlotHostState>() {

    override fun open(
        screenParams: ScreenParams,
        onComplete: (newState: SlotHostState, oldState: SlotHostState) -> Unit
    ) = navigate(
        transformer = {
            if (it.slot == screenParams)
                it.copy()
            else
                SlotHostState(screenParams)
        },
        onComplete = onComplete
    )

    override fun close(
        screenParams: ScreenParams,
        onComplete: (newState: SlotHostState, oldState: SlotHostState) -> Unit
    ) = navigate(
        transformer = {
            if (it.slot == screenParams)
                SlotHostState(null)
            else
                it.copy()
        },
        onComplete = onComplete
    )

    override fun back(state: SlotHostState): (() -> SlotHostState)? {
        if (state.slot == null) return null
        return {
            SlotHostState(null)
        }
    }
}