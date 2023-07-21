package com.example.bottomnav

import com.example.navigation.FeedScreenParams
import com.example.navigation.FeedTabScreenParams
import com.example.navigation.context.ScreenContext
import com.example.navigation.screens.ScreenParams

class FeedTabScreen(context: ScreenContext, params: FeedTabScreenParams): TabHostScreen<FeedTabScreenParams>(context, params) {
    override val initialScreen: ScreenParams get() = FeedScreenParams
}