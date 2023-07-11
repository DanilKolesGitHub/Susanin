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
//
//class VideoScreen(context: ScreenContext, screenType: VideoScreenParams): Screen<VideoScreenParams>(context, screenType) {
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup,
//        params: ScreenParams?
//    ): View {
//        return inflater.inflate(R.layout.video_layout, container, false).also { init(it) }
//    }
//
//    private fun init(container: View) {
//
//    }
//}
//
//fun registerVideoScreens(register: ScreenRegister) {
//    register.registerFactory(VideoScreenParams::class, object : ScreenFactory<VideoScreenParams> {
//        override fun create(screenType: VideoScreenParams, context: ScreenContext): VideoScreen {
//            return VideoScreen(context, screenType)
//        }
//    })
//    register.registerScreen(VideoScreenParams::class, object : ScreenFactory<VideoScreenParams> {
//        override fun create(context: ScreenContext, screenType: VideoScreenParams): VideoScreen {
//            return VideoScreen(context, screenType)
//        }
//    })
//}
