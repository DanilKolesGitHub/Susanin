package com.example.bottomnav

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.navigation.context.ScreenContext
import com.example.navigation.screens.ScreenParams
import com.example.navigation.screens.ViewScreen
import com.example.navigation.stack.StackHostView
import com.example.navigation.stack.stack

abstract class TabHostScreen<P: ScreenParams>(context: ScreenContext, type: P): ViewScreen<P>(context, type) {

    abstract val initialScreen: ScreenParams

    private val stack = stack(initialScreen, true)

    override fun onCreateView(layoutInflater: LayoutInflater, parent: ViewGroup): View {
        return layoutInflater.inflate(R.layout.tab_host_layout, parent, false)
    }

    override fun onViewCreated(view: View) {
        val host: StackHostView = view.findViewById(R.id.tab_host)
        host.observe(stack, viewLifecycle)
    }
}