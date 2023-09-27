package com.example.navigation.stack

import android.os.Parcelable
import com.example.navigation.navigation.DefaultNavigation

internal class StackNavigation<P: Parcelable>: DefaultNavigation<P, StackHostState<P>>() {

    override fun open(
        params: P,
        onComplete: (newState: StackHostState<P>, oldState: StackHostState<P>) -> Unit
    ) = navigate (
            transformer = {
                if (it.stack.contains(params))
                    it.copy(stack = it.stack.dropLastWhile { param -> param != params })
                else
                    it.copy(stack = it.stack + params)
            },
            onComplete = onComplete
        )

    override fun close(
        params: P,
        onComplete: (newState: StackHostState<P>, oldState: StackHostState<P>) -> Unit
    ) = navigate (
            transformer = {
                if (it.stack.size == 1)
                    it.copy()
                else
                    it.copy(stack = it.stack.takeWhile { param -> param != params })
            },
            onComplete = onComplete
        )

    override fun canBack(state: StackHostState<P>): Boolean {
        return state.stack.size > 1
    }

    override fun back(state: StackHostState<P>): StackHostState<P> {
        return StackHostState(stack = state.stack.dropLast(1))
    }
}