package com.example.navigation.stack

import android.os.Parcelable
import com.arkivanov.decompose.router.children.ChildNavState
import com.arkivanov.decompose.router.children.NavState
import com.arkivanov.decompose.router.children.SimpleChildNavState
import kotlinx.android.parcel.Parcelize

@Parcelize
internal data class StackHostState<P : Parcelable>(
    val stack: List<P>
): NavState<P>, Parcelable {

    init {
        require(stack.isNotEmpty()) { "Configuration stack must not be empty" }
    }

    override val children: List<ChildNavState<P>> =
        stack.mapIndexed { index, configuration ->
            SimpleChildNavState(
                configuration = configuration,
                status = if (index == stack.lastIndex)
                    ChildNavState.Status.ACTIVE
                else
                    ChildNavState.Status.INACTIVE,
            )
        }
}

