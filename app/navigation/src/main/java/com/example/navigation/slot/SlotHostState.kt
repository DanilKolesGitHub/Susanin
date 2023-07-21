package com.example.navigation.slot

import android.os.Parcelable
import com.arkivanov.decompose.router.children.ChildNavState
import com.example.navigation.screens.ScreenParams
import com.example.navigation.state.ChildState
import com.example.navigation.state.HostState
import kotlinx.android.parcel.Parcelize

@Parcelize
internal data class SlotHostState(
    internal val slot: ScreenParams?
): HostState, Parcelable {

    override val children: List<ChildState> =
        if (slot == null)
            emptyList()
        else
            listOf(ChildState(configuration = slot, status = ChildNavState.Status.ACTIVE))
}