package com.example.navigation.register

import androidx.annotation.AnyThread
import com.example.navigation.context.ScreenContext
import com.example.navigation.context.ScreenExtensions
import com.example.navigation.factory.ScreenFactory
import com.example.navigation.screens.Screen
import com.example.navigation.screens.ScreenParams
import kotlin.reflect.KClass

abstract class ScreenRegister : NavigationRegister<ScreenParams> {

    /**
     * Регистрирует фабрику для создания экранов.
     * Фабрика регистрируется единожды для каждого экрана.
     * @param type Класс ScreenParams, которому соответствует ScreenComponent.
     * @param factory Фабрика (ScreenContext, ScreenParams) -> ScreenComponent.
     */
    @AnyThread
    abstract fun <P : ScreenParams> registerFactory(type: KClass<P>, factory: ScreenFactory<P>)

    @AnyThread
    inline fun <reified P> registerFactory(
        noinline factory: (params: P, context: ScreenContext) -> Screen<P>,
    ) where P : ScreenParams {
        registerFactory(
            P::class,
            object : ScreenFactory<P> {
                override fun create(params: P, context: ScreenContext): Screen<P> {
                    return factory(params, context)
                }
            },
        )
    }

    internal abstract fun screenExtensions(): ScreenExtensions
}
