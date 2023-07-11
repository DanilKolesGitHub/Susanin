package com.example.navigation.view

import android.animation.Animator
import android.view.View

interface AnimationBehaviour {
    fun close(from: View, to: View): Animator
    fun open(from: View, to: View): Animator
}