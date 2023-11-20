package com.example.susanin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.navigation.DialogScreenParams
import com.example.navigation.MainScreenParams
import com.example.navigation.OverlayScreenParams
import com.example.navigation.SearchScreenParams
import com.example.navigation.TabScreenParams
import com.example.navigation.TestScreenParams
import com.example.navigation.context.ScreenContext
import com.example.navigation.dialogs.dialogs
import com.example.navigation.factory.ScreenFactory
import com.example.navigation.register.ScreenRegister
import com.example.navigation.screens.Screen
import com.example.navigation.screens.ViewScreen
import com.example.navigation.screens.dialogs
import com.example.navigation.screens.slot
import com.example.navigation.screens.stack
import com.example.navigation.slot.SlotHostView
import com.example.navigation.slot.slot
import com.example.navigation.stack.stack

class MainScreen(context: ScreenContext): ViewScreen<MainScreenParams>(context, MainScreenParams) {

    init {
        if (!Initializer.isInitialized()) error("NOT INIT")
    }

    private val stack = stack(initialProvider = {
        listOf(
            OverlayScreenParams(false, 0),
            OverlayScreenParams(false, 1),
            OverlayScreenParams(false, 2),
        )
    })
    private val dialogs = dialogs(null)//DialogScreenParams(Color.BLUE))
    private val slot = slot(OverlayScreenParams(false, 0))//DialogScreenParams(Color.BLUE))

    override fun onCreateView(layoutInflater: LayoutInflater, parent: ViewGroup): View {
        return layoutInflater.inflate(R.layout.activity_main, parent, false)
    }

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)
//        view.findViewById<StackHostView>(R.id.stack).observe(stack, viewLifecycle)
        view.findViewById<SlotHostView>(R.id.slot).observe(slot, viewLifecycle)
//        view.findViewById<DialogsHostView>(R.id.dialogs).observe(dialogs, viewLifecycle)
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
    register.registerFactory(
        OverlayScreenParams::class,
        object : ScreenFactory<OverlayScreenParams> {
            override fun create(
                params: OverlayScreenParams,
                context: ScreenContext
            ): Screen<OverlayScreenParams> {
                return OverlayScreen(context, params)
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
    register.registerNavigation(MainScreenParams::class) {
        stack(TabScreenParams::class, TestScreenParams::class, SearchScreenParams::class)
        dialogs(DialogScreenParams::class)
        slot(OverlayScreenParams::class)
    }
}