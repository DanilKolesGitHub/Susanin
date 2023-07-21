package com.example.video

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.arkivanov.decompose.ComponentContext
import com.example.navigation.*
import com.example.navigation.context.ScreenContext
import com.example.navigation.router.ScreenRegister
import com.example.navigation.screens.Screen
import com.example.navigation.screens.ScreenFactory
import com.example.navigation.screens.ScreenParams
import com.example.navigation.screens.ViewScreen

class VideoScreen(context: ScreenContext, screenType: VideoScreenParams): ViewScreen<VideoScreenParams>(context, screenType) {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
    ): View {
        return inflater.inflate(R.layout.video_layout, container, false)
    }

}

fun registerVideoScreens(register: ScreenRegister) {
    register.registerFactory(VideoScreenParams::class, object : ScreenFactory<VideoScreenParams> {
        override fun create(screenType: VideoScreenParams, context: ScreenContext): VideoScreen {
            return VideoScreen(context, screenType)
        }
    })
}
