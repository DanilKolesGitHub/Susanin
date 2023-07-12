package com.example.navigation.screens

import android.util.Log
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.example.core.dagger.LifecycleLogger
import com.example.navigation.context.ScreenContext
import com.example.navigation.view.ViewRender

abstract class Screen<P: ScreenParams>(context: ScreenContext, params: P):
    ScreenContext by context,
    ViewRender,
    Lifecycle.Callbacks {

    val params = params

    init {
        lifecycle.subscribe(this)
        lifecycle.subscribe(LifecycleLogger("screen ${params}", Log::e))
    }
}
