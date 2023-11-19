package com.example.susanin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.example.navigation.view.ViewRender

class SplashScreen(context: ComponentContext): ComponentContext by context, ViewRender {

    override fun createView(parent: ViewGroup, viewLifecycle: Lifecycle): View {
        return LayoutInflater.from(parent.context).inflate(R.layout.splash_screen, parent, false)
    }
}