package com.example.navigation.view

import android.animation.Animator
import android.animation.ObjectAnimator
import android.view.View

interface AnimationBehaviour {
    fun open(from: View, to: View): Animator
    fun close(from: View, to: View): Animator
}

object ForwardBackwardBehaviour: AnimationBehaviour {

    override fun open(from: View, to: View): Animator {
        return ObjectAnimator.ofFloat(to, View.TRANSLATION_X, from.width.toFloat(), 0f).apply {
            duration = 1000
        }
    }

    override fun close(from: View, to: View): Animator {
        from.bringToFront()
        return ObjectAnimator.ofFloat(from, View.TRANSLATION_X, 0f, from.width.toFloat()).apply {
            duration = 1000
        }
    }
}