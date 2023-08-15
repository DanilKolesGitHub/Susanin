package com.example.navigation.screens

import android.os.Parcelable
import kotlin.reflect.KClass

typealias ScreenType = KClass<out ScreenParams>

interface ScreenParams: Parcelable {

    val key: ScreenType
        get() = this::class
}