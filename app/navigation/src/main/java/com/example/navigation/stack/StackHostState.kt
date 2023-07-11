package com.example.navigation.stack

import com.arkivanov.decompose.router.children.ChildNavState
import com.arkivanov.essenty.parcelable.Parcelable
import com.example.navigation.screens.ScreenParams
import com.example.navigation.state.ChildState
import com.example.navigation.state.HostState
import kotlinx.android.parcel.Parcelize

@Parcelize
internal data class StackHostState(
    val stack: List<ScreenParams>
): HostState, Parcelable {

    init {
        require(stack.isNotEmpty()) { "Configuration stack must not be empty" }
    }

    override val children: List<ChildState> =
        stack.mapIndexed { index, configuration ->
            ChildState(
                configuration = configuration,
                status = if (index == stack.lastIndex)
                    ChildNavState.Status.ACTIVE
                else
                    ChildNavState.Status.INACTIVE,
            )
        }
}

