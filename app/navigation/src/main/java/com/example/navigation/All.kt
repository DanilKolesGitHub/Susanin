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
object TreeScreenParams: ScreenParams

@Parcelize
object StackScreenParams: ScreenParams

@Parcelize
object PagesScreenParams: ScreenParams

@Parcelize
object DialogsScreenParams: ScreenParams

@Parcelize
object SlotScreenParams: ScreenParams

@Parcelize
object PlayerScreenParams: ScreenParams

@Parcelize
object SelectScreenParams: ScreenParams

@Parcelize
data class TestScreenParams(
    val id: Int,
    override val overlay: Boolean = false,
    val animate: Boolean = false,
): ScreenParams, UiParams {

    override val viewTransition: ViewTransition?
        get() = if (animate) SlideViewTransition else null
}
