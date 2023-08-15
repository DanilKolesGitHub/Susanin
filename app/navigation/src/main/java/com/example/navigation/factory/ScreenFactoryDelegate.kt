package com.example.navigation.factory

import com.example.navigation.context.ScreenContext
import com.example.navigation.screens.Screen
import com.example.navigation.screens.ScreenParams
import com.example.navigation.screens.ScreenType

class ScreenFactoryDelegate(
    private val factoryMap: Map<ScreenType, ScreenFactory<ScreenParams>>
) : ScreenFactory<ScreenParams> {

    override fun create(params: ScreenParams, context: ScreenContext): Screen<ScreenParams> {
        if (factoryMap.containsKey(params.key)) {
            return factoryMap[params.key]!!.create(params, context)
        } else
            throw IllegalStateException("Not found factory for ${params.key}")
    }
}