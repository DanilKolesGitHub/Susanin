package com.example.navigation

import androidx.transition.Transition
import com.example.navigation.screens.ScreenParams
import com.example.navigation.view.TopTransition
import com.example.navigation.view.TransitionProvider
import kotlinx.android.parcel.Parcelize

@Parcelize
object MainScreenParams: ScreenParams

@Parcelize
object TabScreenParams: ScreenParams

@Parcelize
object FeedTabScreenParams: ScreenParams, TransitionProvider {
    override val transition: Transition
        get() = TopTransition.transition
}

@Parcelize
object VideoTabScreenParams: ScreenParams

@Parcelize
object TreeTabScreenParams: ScreenParams

@Parcelize
object FeedScreenParams: ScreenParams

@Parcelize
object VideoScreenParams: ScreenParams

@Parcelize
object TreeScreenParams: ScreenParams

@Parcelize
object PlayerScreenParams: ScreenParams

@Parcelize
object SearchScreenParams: ScreenParams

@Parcelize
object InputScreenParams: ScreenParams

@Parcelize
data class ResultScreenParams(val result: String): ScreenParams

@Parcelize
data class DialogScreenParams(val color: Int): ScreenParams

@Parcelize
class SettingsScreenParams(): ScreenParams
