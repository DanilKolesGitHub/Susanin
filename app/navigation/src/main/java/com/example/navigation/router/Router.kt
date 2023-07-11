package com.example.navigation.router

import com.example.navigation.navigation.NavigationNode
import com.example.navigation.navigation.NavigationState

class Router(register: ScreenRegister) {
    internal val screenFactory = ScreenFactoryDelegate(register.factoryMap)
    internal val navigationDispatcher = NavigationDispatcher(
        register.registeredScreens,
        register.defaultParams,
    )
    internal lateinit var navigationState: NavigationState

    fun initRoot(root: NavigationNode) {
        navigationState = NavigationState(root, navigationDispatcher)
    }
}