package com.example.navigation.router

class Router(register: ScreenRegister) {
    internal val screenFactory = ScreenFactoryDelegate(register.factoryMap)
}