package com.example.navigation

import com.example.navigation.screens.ScreenParams
import com.example.navigation.view.AnimationBehaviour
import com.example.navigation.view.AnimationProvider
import com.example.navigation.view.UpBottomBehaviour
import kotlinx.android.parcel.Parcelize

@Parcelize
object MainScreenParams: ScreenParams

@Parcelize
class TabScreenParams(): ScreenParams

@Parcelize
class FeedScreenParams(): ScreenParams

@Parcelize
class VideoScreenParams(): ScreenParams

@Parcelize
class TreeScreenParams(): ScreenParams

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
