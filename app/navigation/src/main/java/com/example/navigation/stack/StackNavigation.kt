package com.example.navigation.stack

import com.example.navigation.navigation.DefaultNavigation
import com.example.navigation.screens.ScreenParams

internal class StackNavigation: DefaultNavigation<StackHostState>() {

    override fun open(
        screenParams: ScreenParams,
        onComplete: (newState: StackHostState, oldState: StackHostState) -> Unit
    ) = navigate (
            transformer = {
                if (it.stack.contains(screenParams))
                    it.copy(stack = it.stack.dropLastWhile { param -> param != screenParams })
                else
                    it.copy(stack = it.stack + screenParams)
            },
            onComplete = onComplete
        )

    override fun close(
        screenParams: ScreenParams,
        onComplete: (newState: StackHostState, oldState: StackHostState) -> Unit
    ) = navigate (
            transformer = {
                if (it.stack.size == 1)
                    it.copy()
                else
                    it.copy(stack = it.stack.takeWhile { param -> param != screenParams })
            },
            onComplete = onComplete
        )

    override fun back(state: StackHostState): (() -> StackHostState)? {
        if (state.stack.size == 1) return null
        return {
            StackHostState(stack = state.stack.dropLast(1))
        }
    }
}