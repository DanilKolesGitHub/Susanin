package com.example.susanin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.children.children
import com.example.navigation.*
import com.example.navigation.context.ScreenContext
import com.example.navigation.router.ScreenRegister
import com.example.navigation.screens.Screen
import com.example.navigation.screens.ScreenFactory
import com.example.navigation.screens.ViewScreen
import com.example.navigation.stack.StackHostView
import com.example.navigation.stack.stack

class MainScreen(context: ScreenContext): ViewScreen<MainScreenParams>(context, MainScreenParams) {

    val stack = stack(
        ResultScreenParams("0")
    )

    override fun onCreateView(layoutInflater: LayoutInflater, parent: ViewGroup): View {
        return layoutInflater.inflate(R.layout.activity_main, parent, false)
    }

    override fun onViewCreated(view: View) {
        val routerView: StackHostView = view.findViewById(R.id.router)
        routerView.observe(stack, lifecycle)
    }

}

fun registerMainScreens(register: ScreenRegister) {
    register.registerDefaultParams(MainScreenParams)
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
    register.registerStackNavigation(MainScreenParams::class, ResultScreenParams::class)
}