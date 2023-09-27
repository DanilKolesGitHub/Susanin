package com.example.navigation.layer

import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.create
import com.arkivanov.essenty.lifecycle.destroy
import com.arkivanov.essenty.lifecycle.pause
import com.arkivanov.essenty.lifecycle.resume
import com.arkivanov.essenty.lifecycle.start
import com.arkivanov.essenty.lifecycle.stop

internal class LayerLifecycle(
    isLocked: Boolean = false
): LifecycleRegistry by LifecycleRegistry(
    if (isLocked) Lifecycle.State.CREATED else Lifecycle.State.RESUMED
) {

    fun lock() {
        moveTo(Lifecycle.State.CREATED)
    }

    fun release() {
        moveTo(Lifecycle.State.RESUMED)
    }

    private fun moveTo(state: Lifecycle.State) {
        when (state) {
            Lifecycle.State.DESTROYED -> moveToDestroyed()
            Lifecycle.State.INITIALIZED -> Unit
            Lifecycle.State.CREATED -> moveToCreated()
            Lifecycle.State.STARTED -> moveToStarted()
            Lifecycle.State.RESUMED -> moveToResumed()
        }
    }

    private fun moveToDestroyed() {
        when (state) {
            Lifecycle.State.DESTROYED -> Unit

            Lifecycle.State.INITIALIZED -> {
                create()
                destroy()
            }

            Lifecycle.State.CREATED,
            Lifecycle.State.STARTED,
            Lifecycle.State.RESUMED -> destroy()
        }
    }

    private fun moveToCreated() {
        when (state) {
            Lifecycle.State.DESTROYED -> Unit
            Lifecycle.State.INITIALIZED -> create()

            Lifecycle.State.CREATED -> Unit

            Lifecycle.State.STARTED,
            Lifecycle.State.RESUMED -> stop()
        }
    }

    private fun moveToStarted() {
        when (state) {
            Lifecycle.State.INITIALIZED,
            Lifecycle.State.CREATED -> start()

            Lifecycle.State.RESUMED -> pause()

            Lifecycle.State.DESTROYED,
            Lifecycle.State.STARTED -> Unit
        }
    }

    private fun moveToResumed() {
        when (state) {
            Lifecycle.State.INITIALIZED,
            Lifecycle.State.CREATED,
            Lifecycle.State.STARTED -> resume()

            Lifecycle.State.RESUMED,
            Lifecycle.State.DESTROYED -> Unit
        }
    }
}
