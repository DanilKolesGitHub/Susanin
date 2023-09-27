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

    override val children: List<ChildNavState<P>>

    init {
        val ch = mutableListOf<ChildNavState<P>>()
        pages.forEachIndexed { index, page ->
            if (index != selected)
                ch.add(SimpleChildNavState(configuration = page, status = ChildNavState.Status.INACTIVE))
        }
        ch.add(SimpleChildNavState(pages[selected], ChildNavState.Status.ACTIVE))
        children = ch
    }
}