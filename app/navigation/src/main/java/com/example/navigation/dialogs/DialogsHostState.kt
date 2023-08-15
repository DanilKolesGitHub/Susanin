package com.example.navigation.dialogs

import android.os.Parcelable
import com.arkivanov.decompose.router.children.ChildNavState
import com.arkivanov.decompose.router.children.NavState
import com.arkivanov.decompose.router.children.SimpleChildNavState
import kotlinx.android.parcel.Parcelize

@Parcelize
internal data class DialogsHostState<P : Parcelable>(
    val dialogs: List<P>
): NavState<P>, Parcelable {

    override val children: List<ChildNavState<P>> =
        dialogs.mapIndexed { index, configuration ->
            SimpleChildNavState(
                configuration = configuration,
                status = if (index == dialogs.lastIndex)
                    ChildNavState.Status.ACTIVE
                else
                    ChildNavState.Status.INACTIVE,
            )
        }
}

