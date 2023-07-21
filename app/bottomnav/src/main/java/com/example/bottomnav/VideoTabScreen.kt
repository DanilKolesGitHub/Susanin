package com.example.bottomnav

import com.example.navigation.VideoScreenParams
import com.example.navigation.VideoTabScreenParams
import com.example.navigation.context.ScreenContext
import com.example.navigation.screens.ScreenParams

class VideoTabScreen(context: ScreenContext, params: VideoTabScreenParams): TabHostScreen<VideoTabScreenParams>(context, params) {
    override val initialScreen: ScreenParams  get() = VideoScreenParams
}