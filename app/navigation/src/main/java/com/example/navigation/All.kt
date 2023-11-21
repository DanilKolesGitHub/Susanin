package com.example.navigation

import com.example.navigation.screens.ScreenParams
import com.example.navigation.view.SlideViewTransition
import com.example.navigation.view.UiParams
import com.example.navigation.view.ViewTransition
import kotlinx.android.parcel.Parcelize

@Parcelize
object MainScreenParams: ScreenParams

@Parcelize
object SplashScreenParams: ScreenParams

@Parcelize
object TabScreenParams: ScreenParams

@Parcelize
object FeedTabScreenParams: ScreenParams

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
data class OverlayScreenParams(
    override val overlay: Boolean,
    val id: Int
): ScreenParams, UiParams {
    override val viewTransition: ViewTransition
        get() = SlideViewTransition
}

@Parcelize
object TestScreenParams: ScreenParams

@Parcelize
data class ResultScreenParams(val result: String): ScreenParams

@Parcelize
data class DialogScreenParams(val color: Int): ScreenParams

@Parcelize
data class NewHostSP(val index: Int): ScreenParams