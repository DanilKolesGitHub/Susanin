package com.example.navigation.router

import com.example.navigation.navigation.NavigationManager
import com.example.navigation.navigation.NavigationState

class Router(register: ScreenRegister, root: NavigationManager) {
    internal val screenFactory = ScreenFactoryDelegate(register.factoryMap)
    internal val navigationDispatcher = NavigationDispatcher(
        register.registeredScreens,
        register.defaultParams,
    )
    internal var navigationState: NavigationState = NavigationState(root, navigationDispatcher)
}