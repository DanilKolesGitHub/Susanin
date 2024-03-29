package com.example.susanin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.navigation.MainScreenParams
import com.example.navigation.SelectScreenParams
import com.example.navigation.TestScreenParams
import com.example.navigation.context.ScreenContext
import com.example.navigation.factory.ScreenFactory
import com.example.navigation.register.ScreenRegister
import com.example.navigation.screens.Screen
import com.example.navigation.screens.ViewScreen
import com.example.navigation.screens.stack
import com.example.navigation.stack.StackHostView

class MainScreen(context: ScreenContext): ViewScreen<MainScreenParams>(context, MainScreenParams) {

    init {
        if (!Initializer.isInitialized()) error("NOT INIT")
    }

    private val stack = stack(
        initialScreen = SelectScreenParams,
    )
    override fun onCreateView(layoutInflater: LayoutInflater, parent: ViewGroup): View {
        return layoutInflater.inflate(R.layout.activity_main, parent, false)
    }

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)
        view.findViewById<StackHostView>(R.id.main_stack).observe(stack, viewLifecycle)
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
        TestScreenParams::class,
        object : ScreenFactory<TestScreenParams> {
            override fun create(
                params: TestScreenParams,
                context: ScreenContext
            ): Screen<TestScreenParams> {
                return TestScreen(context, params)
            }
        }
    )
}