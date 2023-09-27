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

    override fun canBack(state: DialogsHostState<P>): Boolean {
        return state.dialogs.isNotEmpty()
    }

    override fun back(state: DialogsHostState<P>): DialogsHostState<P> {
        return DialogsHostState(dialogs = state.dialogs.dropLast(1))
    }
}