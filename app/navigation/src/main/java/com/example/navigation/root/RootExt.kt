package com.example.navigation.root

import com.arkivanov.decompose.Child
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.children.children
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.parcelable.ParcelableContainer

fun <Instance: Any> ComponentContext.root(
    initialized: () -> Boolean,
    navigation: RootNavigation,
    factory: (params: RootState, context: ComponentContext) -> Instance,
): Value<ChildRoot<RootState, Instance>> {
    return children(
        source = navigation,
        key = "RootNavigation",
        initialState = { RootHostState(initialized()) },
        saveState = { ParcelableContainer(it) },
        restoreState = { RootHostState(initialized()) },
        navTransformer = { state, event -> event.transformer(state) },
        backTransformer = { _ -> null },
        onEventComplete = { _, _, _ -> },
        stateMapper = { _, children ->
            ChildRoot(children.filterIsInstance<Child.Created<RootState, Instance>>().first())
        },
        onStateChanged = { _, _ ->},
        childFactory = factory,
    )
}