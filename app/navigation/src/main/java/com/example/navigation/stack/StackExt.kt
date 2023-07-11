package com.example.navigation.stack

import com.arkivanov.decompose.Child
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.parcelable.ParcelableContainer
import com.arkivanov.essenty.parcelable.consumeRequired
import com.example.navigation.NavigationType
import com.example.navigation.context.ScreenContext
import com.example.navigation.navigation.childScreens
import com.example.navigation.screens.Screen
import com.example.navigation.screens.ScreenParams

fun ScreenContext.stack(
    initial: ScreenParams,
    addInitial: Boolean = false,
    tag: String = NavigationType.STACK.name,
) = stack (
    initial = { listOf(initial) },
    addInitial = addInitial,
    tag = tag,
)

fun ScreenContext.stack(
    initial: () -> List<ScreenParams>,
    addInitial: Boolean = false,
    tag: String = NavigationType.STACK.name,
): Value<ChildStack<ScreenParams, Screen<*>>> {
    val navigationHolder = node.provideNavigation(tag) { pending ->
        val initialScreens = when {
            pending.isNullOrEmpty() -> initial()
            addInitial -> initial() + pending
            else -> pending
        }
        StackNavigationHolder(tag, initialScreens)
    }
    return childScreens(
        navigationHolder = navigationHolder,
        tag = tag,
        stateMapper =  { _, children ->
            @Suppress("UNCHECKED_CAST")
            val createdChildren = children as List<Child.Created<ScreenParams, Screen<*>>>

            ChildStack(
                active = createdChildren.last(),
                backStack = createdChildren.dropLast(1),
            )
        },
        saveState = { ParcelableContainer(it) },
        restoreState = { it.consumeRequired(StackHostState::class) }
    )
}