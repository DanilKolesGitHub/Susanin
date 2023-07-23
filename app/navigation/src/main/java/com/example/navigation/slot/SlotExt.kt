package com.example.navigation.slot

import android.os.Parcelable
import com.arkivanov.decompose.Child
import com.arkivanov.decompose.router.slot.ChildSlot
import com.arkivanov.decompose.value.Value
import com.example.navigation.NavigationType
import com.example.navigation.context.NavigationContext
import com.example.navigation.navigation.children

fun <Params: Parcelable, Instance: Any> NavigationContext.slot(
    initial: Params? = null,
    handleBackButton: Boolean = true,
    tag: String = NavigationType.SLOT.name,
    factory: (params: Params, context: NavigationContext) -> Instance,
) = slot(
    initialProvider = { initial },
    handleBackButton = handleBackButton,
    tag = tag,
    factory = factory
)

fun <Params: Parcelable, Instance: Any> NavigationContext.slot(
    initialProvider: () -> Params?,
    handleBackButton: Boolean = true,
    tag: String = NavigationType.SLOT.name,
    factory: (params: Params, context: NavigationContext) -> Instance,
): Value<ChildSlot<Params, Instance>> {
    val navigationHolder = navigation.provideNavigation(tag) { pending ->
        val initialScreen = when {
            pending.isNullOrEmpty() -> initialProvider()
            else -> pending.lastOrNull()
        }
        SlotNavigationHolder(tag, initialScreen)
    }
    return children(
        navigationHolder = navigationHolder,
        handleBackButton = handleBackButton,
        tag = tag,
        stateMapper =  { _, children ->
            @Suppress("UNCHECKED_CAST")
            val createdChild= children.firstOrNull()  as? Child.Created?
            ChildSlot(createdChild)
        },
        factory
    )
}
