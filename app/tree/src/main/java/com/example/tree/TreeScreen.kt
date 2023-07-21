package com.example.tree

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import com.arkivanov.decompose.ComponentContext
import com.example.navigation.*
import com.example.navigation.context.ScreenContext
import com.example.navigation.router.ScreenRegister
import com.example.navigation.screens.Screen
import com.example.navigation.screens.ScreenFactory
import com.example.navigation.screens.ScreenParams
import com.example.navigation.screens.ViewScreen

class TreeScreen(context: ScreenContext, type: TreeScreenParams): ViewScreen<TreeScreenParams>(context, type) {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
    ): View {
        val layout = ComposeView(container.context)
        layout.setContent {
            TreeView(mock)
        }
        return layout
    }
}

fun registerTreeScreens(register: ScreenRegister) {
    register.registerFactory(TreeScreenParams::class, object : ScreenFactory<TreeScreenParams> {
        override fun create(screenType: TreeScreenParams, context: ScreenContext): TreeScreen {
            return TreeScreen(context, screenType)
        }
    })
}