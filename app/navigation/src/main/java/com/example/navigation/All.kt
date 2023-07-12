package com.example.navigation

import com.example.navigation.screens.ScreenParams
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
class SearchScreenParams(): ScreenParams

@Parcelize
class InputScreenParams(): ScreenParams

@Parcelize
data class ResultScreenParams(val result: String): ScreenParams

@Parcelize
class SettingsScreenParams(): ScreenParams
