package com.example.tree

import com.example.navigation.screens.ScreenParams
import kotlin.reflect.KClass

data class UiNode(val type: KClass<out ScreenParams>,
                  val children : List<UiNode>,
                  val parents: List<KClass<out ScreenParams>>) {
    val name: String get() = type.simpleName?.removeSuffix("ScreenParams")?: "wtf"
}


