package com.example.navigation.pages

import android.os.Parcelable
import com.arkivanov.decompose.router.children.ChildNavState
import com.arkivanov.decompose.router.children.NavState
import com.arkivanov.decompose.router.children.SimpleChildNavState
import kotlinx.android.parcel.Parcelize


@Parcelize
internal data class PagesHostState<P : Parcelable>(
    internal val pages: List<P>,
    internal val selected: Int,
): NavState<P>, Parcelable {

    override val children: List<ChildNavState<P>> =
        pages.mapIndexed { index, page ->
            SimpleChildNavState(
                configuration = page,
                status = if (index == selected)
                    ChildNavState.Status.ACTIVE
                else
                    ChildNavState.Status.INACTIVE
            )
        }
}