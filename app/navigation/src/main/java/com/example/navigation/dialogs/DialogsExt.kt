package com.example.navigation.dialogs

import android.os.Parcelable
import com.arkivanov.decompose.Child
import com.arkivanov.decompose.value.Value
import com.example.navigation.NavigationType
import com.example.navigation.context.NavigationContext
import com.example.navigation.dispatcher.Type
import com.example.navigation.navigation.children
import com.example.navigation.register.HierarchyBuilder

fun <Params : Parcelable> HierarchyBuilder<Params>.dialogs(vararg types: Type<Params>) =
    navigation(NavigationType.DIALOGS.name, *types)

fun <Params : Parcelable, Instance : Any> NavigationContext<Params>.dialogs(
    initial: Params,
    handleBackButton: Boolean = true,
    tag: String? = null,
    factory: (params: Params, context: NavigationContext<Params>) -> Instance,
) = dialogs(
    initialProvider = { listOf(initial) },
    handleBackButton = handleBackButton,
    tag = tag,
    factory,
)

fun <Params : Parcelable, Instance : Any> NavigationContext<Params>.dialogs(
    initialProvider: () -> List<Params>,
    handleBackButton: Boolean = true,
    tag: String? = null,
    factory: (params: Params, context: NavigationContext<Params>) -> Instance,
): Value<ChildDialogs<Params, Instance>> {
    val tag = tag ?: NavigationType.DIALOGS.name
    val navigationHolder = navigation.provideHolder(tag) { pending ->
        val pendingStack: List<Params>? = pending?.toList()
        val initialScreens = when {
            pendingStack.isNullOrEmpty() -> initialProvider()
            else -> pendingStack
        }
        DialogsNavigationHolder(tag, initialScreens)
    }
    return children(
        navigationHolder = navigationHolder,
        handleBackButton = handleBackButton,
        tag = tag,
        stateMapper = { _, children ->
            @Suppress("UNCHECKED_CAST")
            val createdChildren = children as List<Child.Created<Params, Instance>>
            ChildDialogs(
                dialogs = createdChildren
            )
        },
        factory,
    )
}
