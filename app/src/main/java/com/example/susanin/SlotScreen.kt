package com.example.susanin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.navigation.SelectScreenParams
import com.example.navigation.SlotScreenParams
import com.example.navigation.TestScreenParams
import com.example.navigation.context.ScreenContext
import com.example.navigation.factory.ScreenFactory
import com.example.navigation.pages.pages
import com.example.navigation.register.ScreenRegister
import com.example.navigation.screens.Screen
import com.example.navigation.screens.ViewScreen
import com.example.navigation.screens.slot
import com.example.navigation.slot.SlotHostView
import com.example.navigation.slot.slot

class SlotScreen(context: ScreenContext, params: SlotScreenParams): ViewScreen<SlotScreenParams>(context, params) {

    init {
        if (!Initializer.isInitialized()) error("NOT INIT")
    }

    private val slot = slot(
        initialSlot = TestScreenParams(0),
    )
    override fun onCreateView(layoutInflater: LayoutInflater, parent: ViewGroup): View {
        return layoutInflater.inflate(R.layout.slot_screen, parent, false)
    }

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)
        (view as SlotHostView).observe(slot, viewLifecycle)
    }
}

fun registerSlotScreens(
    register: ScreenRegister,
) {
    register.registerFactory(
        SlotScreenParams::class,
        object : ScreenFactory<SlotScreenParams> {
            override fun create(
                params: SlotScreenParams,
                context: ScreenContext
            ): Screen<SlotScreenParams> {
                return SlotScreen(context, params)
            }
        }
    )
    register.registerNavigation(SelectScreenParams) {
        pages(SlotScreenParams::class)
    }
    register.registerNavigation(SlotScreenParams) {
        slot(TestScreenParams::class)
    }
}