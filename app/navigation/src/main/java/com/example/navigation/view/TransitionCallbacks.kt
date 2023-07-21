package com.example.navigation.view

import androidx.transition.Transition
import androidx.transition.Transition.TransitionListener

internal fun Transition.addCallbacks(
    onStart: ((transition: Transition) -> Unit)? = null,
    onEnd: ((transition: Transition) -> Unit)? = null,
    onCancel: ((transition: Transition) -> Unit)? = null,
    onPause: ((transition: Transition) -> Unit)? = null,
    onResume: ((transition: Transition) -> Unit)? = null,
) {
    this.addListener(TransitionCallbacks(onStart, onEnd, onCancel, onPause, onResume))
}

private class TransitionCallbacks(
    private val onStart: ((transition: Transition) -> Unit)? = null,
    private val onEnd: ((transition: Transition) -> Unit)? = null,
    private val onCancel: ((transition: Transition) -> Unit)? = null,
    private val onPause: ((transition: Transition) -> Unit)? = null,
    private val onResume: ((transition: Transition) -> Unit)? = null,
): TransitionListener {
    override fun onTransitionStart(transition: Transition) {
        onStart?.invoke(transition)
    }

    override fun onTransitionEnd(transition: Transition) {
        onEnd?.invoke(transition)
        transition.removeListener(this)
    }

    override fun onTransitionCancel(transition: Transition) {
        onCancel?.invoke(transition)
        transition.removeListener(this)
    }

    override fun onTransitionPause(transition: Transition) {
        onPause?.invoke(transition)
    }

    override fun onTransitionResume(transition: Transition) {
        onResume?.invoke(transition)
    }
}