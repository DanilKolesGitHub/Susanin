package com.example.navigation.screens

import android.os.Parcelable
import kotlin.reflect.KClass

typealias ScreenKey = KClass<out ScreenParams>

interface ScreenParams: Parcelable {

    val key: ScreenKey
        get() = this::class
}