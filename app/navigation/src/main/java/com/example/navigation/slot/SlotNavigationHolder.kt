package com.example.navigation.slot

import android.os.Parcelable
import com.example.navigation.navigation.NavigationHolder

internal class SlotNavigationHolder<P : Parcelable>(
    tag: String,
    initial: P?
): NavigationHolder<P, SlotHostState<P>>(
    tag,
    SlotNavigation(),
    SlotHostState(initial)
)