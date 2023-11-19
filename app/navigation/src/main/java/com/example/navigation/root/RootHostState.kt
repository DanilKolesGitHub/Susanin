package com.example.navigation.root

import android.os.Parcelable
import com.arkivanov.decompose.router.children.ChildNavState
import com.arkivanov.decompose.router.children.NavState
import com.arkivanov.decompose.router.children.SimpleChildNavState
import kotlinx.android.parcel.Parcelize

@Parcelize
data class RootHostState(
    val initialized: Boolean,
): NavState<RootState>, Parcelable {

    override val children: List<ChildNavState<RootState>> = listOf(
        SimpleChildNavState(RootState.Splash, if (initialized) ChildNavState.Status.DESTROYED else ChildNavState.Status.ACTIVE),
        SimpleChildNavState(RootState.Content, if (initialized) ChildNavState.Status.ACTIVE else ChildNavState.Status.DESTROYED),
    )
}
@Parcelize
enum class RootState : Parcelable { Splash, Content }