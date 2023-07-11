package com.example.navigation.router

import com.example.navigation.context.ScreenContext
import com.example.navigation.screens.Screen
import com.example.navigation.screens.ScreenFactory
import com.example.navigation.screens.ScreenKey
import com.example.navigation.screens.ScreenParams
import kotlin.reflect.KClass

class ScreenFactoryDelegate(
    private val factoryMap: Map<ScreenKey, ScreenFactory<ScreenParams>>
) : ScreenFactory<ScreenParams> {

    override fun create(params: ScreenParams, context: ScreenContext): Screen<ScreenParams> {
        if (factoryMap.containsKey(params.key)) {
            return factoryMap[params.key]!!.create(params, context)
        } else
            throw IllegalStateException("Not found factory for ${params.key}")
    }
}