package com.example.bottomnav

import com.example.navigation.TreeScreenParams
import com.example.navigation.TreeTabScreenParams
import com.example.navigation.context.ScreenContext
import com.example.navigation.screens.ScreenParams

class TreeTabScreen(context: ScreenContext, params: TreeTabScreenParams): TabHostScreen<TreeTabScreenParams>(context, params) {
    override val initialScreen: ScreenParams get() = TreeScreenParams
}