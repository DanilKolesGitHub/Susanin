package com.example.feed

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.arkivanov.decompose.ComponentContext
import com.example.navigation.*
import com.example.navigation.context.ScreenContext
import com.example.navigation.screens.Screen
import com.example.navigation.screens.ScreenFactory
import com.example.navigation.screens.ScreenParams
//
//class FeedScreen(context: ScreenContext, type: FeedScreenParams): Screen<FeedScreenParams>(context, type) {
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup,
//        params: ScreenParams?
//    ): View {
//        return inflater.inflate(R.layout.feed_layout, container, false).also { init(it) }
//    }
//
//    private fun init(container: View) {
//
//    }
//}
//
//fun registerFeedScreens(register: ScreenRegister) {
//    register.registerScreen(FeedScreenParams::class, object : ScreenFactory<FeedScreenParams> {
//        override fun create(context: ScreenContext, type: FeedScreenParams): FeedScreen {
//            return FeedScreen(context, type)
//        }
//    })
//}
