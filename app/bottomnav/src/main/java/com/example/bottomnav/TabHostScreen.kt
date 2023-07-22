package com.example.bottomnav

import com.example.navigation.context.ScreenContext
import com.example.navigation.screens.ScreenParams
import com.example.navigation.screens.ViewScreen

abstract class TabHostScreen<P: ScreenParams>(context: ScreenContext, type: P): ViewScreen<P>(context, type) {

}