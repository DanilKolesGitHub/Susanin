package com.example.navigation.dialogs

import android.os.Parcelable
import com.example.navigation.navigation.DefaultNavigation

internal class DialogsNavigation<P: Parcelable>: DefaultNavigation<P, DialogsHostState<P>>() {

    override fun open(
        params: P,
        onComplete: (newState: DialogsHostState<P>, oldState: DialogsHostState<P>) -> Unit
    ) = navigate (
        transformer = {
            if (it.dialogs.contains(params))
                it.copy(dialogs = it.dialogs.dropLastWhile { param -> param != params })
            else
                it.copy(dialogs = it.dialogs + params)
        },
        onComplete = onComplete
    )

    override fun close(
        params: P,
        onComplete: (newState: DialogsHostState<P>, oldState: DialogsHostState<P>) -> Unit
    ) = navigate (
        transformer = {
            it.copy(dialogs = it.dialogs.takeWhile { param -> param != params })
        },
        onComplete = onComplete
    )

    override fun back(state: DialogsHostState<P>): (() -> DialogsHostState<P>)? {
        if (state.dialogs.isEmpty()) return null
        return {
            DialogsHostState(dialogs = state.dialogs.dropLast(1))
        }
    }
}