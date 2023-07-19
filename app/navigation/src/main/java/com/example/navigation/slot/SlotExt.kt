package com.example.navigation.slot

import com.arkivanov.decompose.Child
import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.parcelable.ParcelableContainer
import com.arkivanov.essenty.parcelable.consumeRequired
import com.example.navigation.NavigationType
import com.example.navigation.context.ScreenContext
import com.example.navigation.navigation.childScreens
import com.example.navigation.screens.Screen
import com.example.navigation.screens.ScreenParams

fun ScreenContext.slot(
    initial: ScreenParams?,
    handleBackButton: Boolean = true,
    tag: String = NavigationType.SLOT.name,
) = slot (
    initial = { initial },
    handleBackButton = handleBackButton,
    tag = tag,
)

fun ScreenContext.slot(
    initial: () -> ScreenParams?,
    handleBackButton: Boolean = true,
    tag: String = NavigationType.SLOT.name,
): Value<ChildSlot<ScreenParams, Screen<*>>> {
    val navigationHolder = node.provideNavigation(tag) { pending ->
        val initialScreen = when {
            pending.isNullOrEmpty() -> initial()
            else -> pending.lastOrNull()
        }
        SlotNavigationHolder(tag, initialScreen)
    }
    return childScreens(
        navigationHolder = navigationHolder,
        handleBackButton = handleBackButton,
        tag = tag,
        stateMapper =  { _, children ->
            @Suppress("UNCHECKED_CAST")
            val createdChild= children.firstOrNull()  as? Child.Created?
            ChildSlot(createdChild)
        },
        saveState = { ParcelableContainer(it) },
        restoreState = { it.consumeRequired(SlotHostState::class) }
    )
}
