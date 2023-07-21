package com.example.navigation

import com.example.navigation.screens.ScreenParams
import com.example.navigation.view.AnimationBehaviour
import com.example.navigation.view.AnimationProvider
import com.example.navigation.view.UpBottomBehaviour
import kotlinx.android.parcel.Parcelize

@Parcelize
object MainScreenParams: ScreenParams

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
class PlayerScreenParams(): ScreenParams

@Parcelize
object SearchScreenParams: ScreenParams

@Parcelize
object InputScreenParams: ScreenParams

@Parcelize
data class ResultScreenParams(val result: String): ScreenParams, AnimationProvider{
    override val animationBehaviour: AnimationBehaviour = UpBottomBehaviour
}

@Parcelize
class SettingsScreenParams(): ScreenParams
