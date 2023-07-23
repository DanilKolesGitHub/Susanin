package com.example.navigation.pages

import android.os.Parcelable
import com.example.navigation.navigation.NavigationHolder

internal class PagesNavigationHolder<P : Parcelable>(
    tag: String,
    pages: List<P>,
    selected: Int,
    closeBehaviour: PagesNavigation.CloseBehaviour,
    backBehaviour: PagesNavigation.BackBehaviour,
): NavigationHolder<P, PagesHostState<P>>(
    tag,
    PagesNavigation(closeBehaviour, backBehaviour),
    PagesHostState(pages, selected)
)