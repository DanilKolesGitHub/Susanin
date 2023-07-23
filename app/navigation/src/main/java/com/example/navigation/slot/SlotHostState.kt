package com.example.navigation.slot

import android.os.Parcelable
import com.arkivanov.decompose.router.children.ChildNavState
import com.arkivanov.decompose.router.children.NavState
import com.arkivanov.decompose.router.children.SimpleChildNavState
import kotlinx.android.parcel.Parcelize

@Parcelize
internal data class SlotHostState<P : Parcelable>(
    internal val slot: P?
): NavState<P>, Parcelable {

    override val children: List<ChildNavState<P>> =
        if (slot == null)
            emptyList()
        else
            listOf(SimpleChildNavState(configuration = slot, status = ChildNavState.Status.ACTIVE))
}