package com.example.navigation.view

import android.animation.Animator
import android.animation.ObjectAnimator
import android.view.View
import android.view.ViewGroup
import com.arkivanov.essenty.parcelable.Parcelable
import kotlinx.android.parcel.Parcelize

interface AnimationBehaviour: Parcelable {
    fun open(from: View, to: View, parent: ViewGroup): Animator
    fun close(from: View, to: View, parent: ViewGroup): Animator
}

@Parcelize
object ForwardBackwardBehaviour: AnimationBehaviour {

    override fun open(from: View, to: View, parent: ViewGroup): Animator {
        return ObjectAnimator.ofFloat(to, View.TRANSLATION_X, parent.width.toFloat(), 0f).apply {
            duration = 1000
        }
    }

    override fun close(from: View, to: View, parent: ViewGroup): Animator {
        return ObjectAnimator.ofFloat(from, View.TRANSLATION_X, 0f, parent.width.toFloat()).apply {
            duration = 1000
        }
    }
}

@Parcelize
object UpBottomBehaviour: AnimationBehaviour {

    override fun open(from: View, to: View, parent: ViewGroup): Animator {
        return ObjectAnimator.ofFloat(to, View.TRANSLATION_Y, parent.height.toFloat(), 0f).apply {
            duration = 1000
        }
    }

    override fun close(from: View, to: View, parent: ViewGroup): Animator {
        return ObjectAnimator.ofFloat(from, View.TRANSLATION_Y, 0f, parent.height.toFloat()).apply {
            duration = 1000
        }
    }
}

@Parcelize
object TopBehaviour: AnimationBehaviour {

    override fun open(from: View, to: View, parent: ViewGroup): Animator {
        return ObjectAnimator.ofFloat(to, View.ALPHA, 0f, 1f).apply {
            duration = 1000
        }
    }

    override fun close(from: View, to: View, parent: ViewGroup): Animator {
        return ObjectAnimator.ofFloat(from, View.ALPHA, 1f, 0f).apply {
            duration = 1000
        }
    }
}