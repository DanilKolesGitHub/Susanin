package com.example.susanin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.navigation.SelectScreenParams
import com.example.navigation.StackScreenParams
import com.example.navigation.TestScreenParams
import com.example.navigation.context.ScreenContext
import com.example.navigation.factory.ScreenFactory
import com.example.navigation.pages.pages
import com.example.navigation.register.ScreenRegister
import com.example.navigation.screens.Screen
import com.example.navigation.screens.ViewScreen
import com.example.navigation.screens.stack
import com.example.navigation.stack.StackHostView
import com.example.navigation.stack.stack


class StackScreen(context: ScreenContext, params: StackScreenParams): ViewScreen<StackScreenParams>(context, params) {

    init {
        if (!Initializer.isInitialized()) error("NOT INIT")
    }

    private val stack = stack(
        initialScreen = TestScreenParams(0),
    )
    override fun onCreateView(layoutInflater: LayoutInflater, parent: ViewGroup): View {
        return layoutInflater.inflate(R.layout.stack_screen, parent, false)

    }

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)
        (view as StackHostView).observe(stack, viewLifecycle)
    }
}

fun registerStackScreens(
    register: ScreenRegister,
) {
    register.registerFactory(
        StackScreenParams::class,
        object : ScreenFactory<StackScreenParams> {
            override fun create(
                params: StackScreenParams,
                context: ScreenContext
            ): Screen<StackScreenParams> {
                return StackScreen(context, params)
            }
        }
    )
    register.registerNavigation(SelectScreenParams) {
        pages(StackScreenParams::class)
    }
    register.registerNavigation(StackScreenParams) {
        stack(TestScreenParams::class)
    }
}