package com.example.susanin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.navigation.DialogScreenParams
import com.example.navigation.InputScreenParams
import com.example.navigation.MainScreenParams
import com.example.navigation.SearchScreenParams
import com.example.navigation.TabScreenParams
import com.example.navigation.context.ScreenContext
import com.example.navigation.dialogs.dialogs
import com.example.navigation.factory.ScreenFactory
import com.example.navigation.register.ScreenRegister
import com.example.navigation.screens.Screen
import com.example.navigation.screens.ViewScreen
import com.example.navigation.stack.stack

class MainScreen(context: ScreenContext): ViewScreen<MainScreenParams>(context, MainScreenParams) {


    override fun onCreateView(layoutInflater: LayoutInflater, parent: ViewGroup): View {
        return layoutInflater.inflate(R.layout.activity_main, parent, false)
    }

}

fun registerMainScreens(
    register: ScreenRegister,
) {
    register.registerFactory(
        MainScreenParams::class,
        object : ScreenFactory<MainScreenParams> {
            override fun create(
                params: MainScreenParams,
                context: ScreenContext
            ): Screen<MainScreenParams> {
                return MainScreen(context)
            }
        }
    )
    register.registerFactory(
        DialogScreenParams::class,
        object : ScreenFactory<DialogScreenParams> {
            override fun create(
                params: DialogScreenParams,
                context: ScreenContext
            ): Screen<DialogScreenParams> {
                return DialogScreen(context, params)
            }
        }
    )
    register.registerNavigation(MainScreenParams::class){
        stack(TabScreenParams::class, SearchScreenParams::class, InputScreenParams::class)
        dialogs(DialogScreenParams::class)
    }
}