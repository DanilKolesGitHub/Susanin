package com.example.navigation.screens

import com.example.navigation.context.ScreenContext

interface ScreenFactory<T: ScreenParams> {
    fun create(params: T, context: ScreenContext): Screen<T>
}