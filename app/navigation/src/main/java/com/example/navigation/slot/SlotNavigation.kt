package com.example.navigation.slot

import android.os.Parcelable
import com.example.navigation.navigation.DefaultNavigation

internal class SlotNavigation<P: Parcelable> : DefaultNavigation<P, SlotHostState<P>>() {

    override fun open(
        screenParams: P,
        onComplete: (newState: SlotHostState<P>, oldState: SlotHostState<P>) -> Unit
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
        params: P,
        onComplete: (newState: SlotHostState<P>, oldState: SlotHostState<P>) -> Unit
    ) = navigate(
        transformer = {
            if (it.slot == params)
                SlotHostState(null)
            else
                it.copy()
        },
        onComplete = onComplete
    )

    override fun canBack(state: SlotHostState<P>): Boolean {
        return state.slot != null
    }

    override fun back(state: SlotHostState<P>): SlotHostState<P> {
        return SlotHostState(null)
    }
}