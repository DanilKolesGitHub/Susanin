package com.example.susanin.root

import com.arkivanov.decompose.Child
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.children.children
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.parcelable.ParcelableContainer

fun <Instance: Any, Ui: Any> ComponentContext.root(
    initialized: () -> Boolean,
    navigation: RootNavigation,
    stateMapper: (state: RootHostState, children: List<Child<RootState, Instance>>) -> Ui,
    factory: (params: RootState, context: ComponentContext) -> Instance,
): Value<Ui> {
    return children(
        source = navigation,
        key = "RootNavigation",
        initialState = { RootHostState(initialized()) },
        saveState = { ParcelableContainer(it) },
        restoreState = { RootHostState(initialized()) },
        navTransformer = { state, event -> event.transformer(state) },
        backTransformer = { _ -> null },
        onEventComplete = { _, _, _ -> },
        stateMapper = stateMapper,
        onStateChanged = { _, _ ->},
        childFactory = factory,
    )
}