package com.example.navigation.stack

import android.os.Parcelable
import com.example.navigation.navigation.NavigationHolder

internal class StackNavigationHolder<P: Parcelable>(
    tag: String,
    initial: List<P>
): NavigationHolder<P, StackHostState<P>>(
    tag,
    StackNavigation(),
    StackHostState(initial)
)