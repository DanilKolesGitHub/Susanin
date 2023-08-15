package com.example.navigation.factory

import com.example.navigation.context.ScreenContext
import com.example.navigation.screens.Screen
import com.example.navigation.screens.ScreenParams

interface ScreenFactory<T: ScreenParams> {
    fun create(params: T, context: ScreenContext): Screen<T>
}