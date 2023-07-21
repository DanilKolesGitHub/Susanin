package com.example.navigation.view

import android.os.Parcelable
import android.view.Gravity
import androidx.transition.Fade
import androidx.transition.Slide
import androidx.transition.Transition
import kotlinx.android.parcel.Parcelize

interface TransitionProvider: Parcelable {

    val transition: Transition
}

@Parcelize
object TopTransition: TransitionProvider {
    override val transition: Transition
        get() = Fade().apply {
            duration = 500
        }
}

@Parcelize
object ForwardBackwardTransition: TransitionProvider {
    override val transition: Transition
        get() = Slide(Gravity.END).apply {
            duration = 500
        }
}

@Parcelize
object BottomUpTransition: TransitionProvider {
    override val transition: Transition
        get() = Slide(Gravity.BOTTOM).apply {
            duration = 500
        }
}