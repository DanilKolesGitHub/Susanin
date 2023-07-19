package com.example.navigation.view

import android.view.View
import android.view.ViewGroup
import com.arkivanov.essenty.lifecycle.Lifecycle

interface ViewRender {
    fun createView(parent: ViewGroup, viewLifecycle: Lifecycle): View
}