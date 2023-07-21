package com.example.navigation.pages

import android.os.Parcelable
import com.arkivanov.decompose.router.children.ChildNavState
import com.example.navigation.screens.ScreenParams
import com.example.navigation.state.ChildState
import com.example.navigation.state.HostState
import kotlinx.android.parcel.Parcelize


@Parcelize
internal data class PagesHostState(
    internal val pages: List<ScreenParams>,
    internal val selected: Int,
): HostState, Parcelable {

    override val children: List<ChildState> =
        pages.mapIndexed { index, page ->
            ChildState(
                configuration = page,
                status = if (index == selected)
                    ChildNavState.Status.ACTIVE
                else
                    ChildNavState.Status.INACTIVE
            )
        }
}