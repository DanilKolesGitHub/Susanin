package com.example.navigation.router

import com.example.navigation.NavigationType
import com.example.navigation.context.ScreenContext
import com.example.navigation.screens.Screen
import com.example.navigation.screens.ScreenFactory
import com.example.navigation.screens.ScreenKey
import com.example.navigation.screens.ScreenParams
import kotlin.reflect.KClass

class ScreenRegister {

    internal val defaultParams: MutableMap<ScreenKey, ScreenParams> = HashMap()
    internal val registeredScreens: MutableMap<ScreenKey, MutableMap<String, MutableSet<ScreenKey>>> = HashMap()
    internal val factoryMap: MutableMap<ScreenKey, ScreenFactory<ScreenParams>> = HashMap()

    fun <T : ScreenParams> registerFactory(key: KClass<T>, factory: ScreenFactory<T>) {
        if (factoryMap.containsKey(key)) {
            return
        }
        factoryMap[key] = factory as ScreenFactory<ScreenParams>
    }

    inline fun <reified P> registerFactory(
        type: KClass<P>,
        noinline factory: (context: ScreenContext, params: P) -> Screen<P>,
    ) where P : ScreenParams {
        registerFactory(
            type,
            object : ScreenFactory<P> {
                override fun create(params: P, context: ScreenContext): Screen<P> {
                    return factory(context, params)
                }
            },
        )
    }

    fun <T : ScreenParams> registerDefaultParams(params: T) {
        if (defaultParams.containsKey(params.key)) {
            return
        }
        defaultParams[params.key] = params
    }

    fun registerNavigation(host: ScreenKey, tag: String, vararg params: ScreenKey) {
        registeredScreens
            .getOrPut(host) { HashMap(1) }
            .getOrPut(tag) { HashSet(params.size) }
            .addAll(params)
    }

    fun registerStackNavigation(host: ScreenKey, vararg params: ScreenKey) {
        registerNavigation(host, NavigationType.STACK.name, *params)
    }
}