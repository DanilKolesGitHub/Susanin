package com.example.feed

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

class FeedScreen(context: ScreenContext, type: FeedScreenParams): ViewScreen<FeedScreenParams>(context, type) {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
    ): View {
        return inflater.inflate(R.layout.feed_layout, container, false)
    }
}

fun registerFeedScreens(register: ScreenRegister) {
    register.registerFactory(FeedScreenParams::class, object : ScreenFactory<FeedScreenParams> {
        override fun create(type: FeedScreenParams, context: ScreenContext): FeedScreen {
            return FeedScreen(context, type)
        }
    })
}
