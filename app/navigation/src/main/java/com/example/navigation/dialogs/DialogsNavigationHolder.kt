package com.example.navigation.dialogs

import com.arkivanov.essenty.parcelable.Parcelable
import com.example.navigation.navigation.NavigationHolder

internal class DialogsNavigationHolder<P: Parcelable>(
    tag: String,
    initial: List<P>
): NavigationHolder<P, DialogsHostState<P>>(
    tag,
    DialogsNavigation(),
    DialogsHostState(initial)
)