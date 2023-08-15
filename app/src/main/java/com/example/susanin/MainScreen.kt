package com.example.susanin

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.navigation.DialogScreenParams
import com.example.navigation.MainScreenParams
import com.example.navigation.TabScreenParams
import com.example.navigation.context.ScreenContext
import com.example.navigation.dialogs.DialogsHostView
import com.example.navigation.dialogs.dialogs
import com.example.navigation.factory.ScreenFactory
import com.example.navigation.register.ScreenRegister
import com.example.navigation.screens.Screen
import com.example.navigation.screens.ViewScreen
import com.example.navigation.screens.dialogs
import com.example.navigation.screens.stack
import com.example.navigation.stack.StackHostView
import com.example.navigation.stack.stack
import com.example.navigation.view.BottomUpTransition
import com.example.navigation.view.ForwardBackwardTransition

class MainScreen(context: ScreenContext): ViewScreen<MainScreenParams>(context, MainScreenParams) {

    val stack = stack(
        TabScreenParams
    )

    val dialogs = dialogs(DialogScreenParams(Color.BLUE))

    override fun onCreateView(layoutInflater: LayoutInflater, parent: ViewGroup): View {
        return layoutInflater.inflate(R.layout.activity_main, parent, false)
    }

    override fun onViewCreated(view: View) {
        val routerView: StackHostView = view.findViewById(R.id.stack)
        routerView.observe(stack, viewLifecycle, transitionProvider = ForwardBackwardTransition)
        val slotView: DialogsHostView = view.findViewById(R.id.slot)
        slotView.observe(dialogs, viewLifecycle, transitionProvider = BottomUpTransition)
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
        stack(TabScreenParams::class)
        dialogs(DialogScreenParams::class)
    }
}