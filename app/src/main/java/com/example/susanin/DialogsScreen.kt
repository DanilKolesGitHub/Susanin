package com.example.susanin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.navigation.DialogsScreenParams
import com.example.navigation.SelectScreenParams
import com.example.navigation.TestScreenParams
import com.example.navigation.context.ScreenContext
import com.example.navigation.dialogs.DialogsHostView
import com.example.navigation.dialogs.dialogs
import com.example.navigation.factory.ScreenFactory
import com.example.navigation.pages.pages
import com.example.navigation.register.ScreenRegister
import com.example.navigation.screens.Screen
import com.example.navigation.screens.ViewScreen
import com.example.navigation.screens.dialogs

class DialogsScreen(context: ScreenContext, params: DialogsScreenParams): ViewScreen<DialogsScreenParams>(context, params) {

    init {
        if (!Initializer.isInitialized()) error("NOT INIT")
    }

    private val dialogs = dialogs(
        initialScreen = TestScreenParams(0),
    )
    override fun onCreateView(layoutInflater: LayoutInflater, parent: ViewGroup): View {
        return layoutInflater.inflate(R.layout.dialogs_screen, parent, false)

    }

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)
        (view as DialogsHostView).observe(dialogs, viewLifecycle)
    }
}

fun registerDialogsScreens(
    register: ScreenRegister,
) {
    register.registerFactory(
        DialogsScreenParams::class,
        object : ScreenFactory<DialogsScreenParams> {
            override fun create(
                params: DialogsScreenParams,
                context: ScreenContext
            ): Screen<DialogsScreenParams> {
                return DialogsScreen(context, params)
            }
        }
    )
    register.registerNavigation(SelectScreenParams) {
        pages(DialogsScreenParams::class)
    }
    register.registerNavigation(DialogsScreenParams) {
        dialogs(TestScreenParams::class)
    }
}