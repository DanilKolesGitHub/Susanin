package com.example.navigation.register

import com.example.navigation.context.ScreenExtensions
import com.example.navigation.factory.ScreenFactory
import com.example.navigation.factory.ScreenFactoryDelegate
import com.example.navigation.screens.ScreenParams
import com.example.navigation.screens.ScreenType
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

class ScreenRegisterImpl : ScreenRegister(), NavigationRegister<ScreenParams> by NavigationRegisterImpl() {

    private val factoryMap = ConcurrentHashMap<ScreenType, ScreenFactory<ScreenParams>>()

    override fun <P : ScreenParams> registerFactory(type: KClass<P>, factory: ScreenFactory<P>) {
        if (factoryMap.put(type, factory as ScreenFactory<ScreenParams>) != null) {
            error("Factory for ${type.simpleName} already registered.")
        }
    }

    override fun screenExtensions(): ScreenExtensions {
        return ScreenExtensions(
            ScreenFactoryDelegate(HashMap(factoryMap)),
        )
    }
}
