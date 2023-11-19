package com.example.susanin

import kotlinx.coroutines.delay
import java.util.concurrent.atomic.AtomicBoolean

object Initializer {

    private var atomicBoolean = AtomicBoolean(false)

    fun isInitialized() = atomicBoolean.get()

    suspend fun init(){
        delay(200)
        atomicBoolean.set(true)
    }
}