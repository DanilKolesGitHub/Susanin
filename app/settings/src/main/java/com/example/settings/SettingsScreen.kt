package com.example.settings

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import com.arkivanov.decompose.router.stack.childStack
import com.example.navigation.*
import com.example.navigation.context.ScreenContext
import com.example.navigation.router.ScreenRegister
import com.example.navigation.screens.Screen
import com.example.navigation.screens.ScreenFactory
import com.example.navigation.screens.ScreenParams
//
//class SettingsScreen(context: ScreenContext,
//                     screenType: SettingsScreenParams)
//    : Screen<SettingsScreenParams>(context, screenType) {
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup,
//        params: ScreenParams?
//    ): View {
//        childContext()
//        val layout = ComposeView(container.context)
//        layout.setContent {
//            SettingsView(this)
//        }
//        return layout
//    }
//
//    fun expand() {
//
//    }
//
//    fun close() {
//
//    }
//
//}
//
//fun registerSettingsScreens(register: ScreenRegister) {
//    register.registerScreen(SettingsScreenParams::class, object : ScreenFactory<SettingsScreenParams> {
//        override fun create(context: ScreenContext, screenType: SettingsScreenParams): SettingsScreen {
//            return SettingsScreen(context, screenType)
//        }
//    })
//}