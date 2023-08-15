package com.example.navigation.stack

import android.os.Parcelable
import com.arkivanov.decompose.Child
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import com.example.navigation.NavigationType
import com.example.navigation.context.NavigationContext
import com.example.navigation.dispatcher.Type
import com.example.navigation.navigation.children
import com.example.navigation.register.HierarchyBuilder

fun <Params : Parcelable> HierarchyBuilder<Params>.stack(vararg types: Type<Params>) =
    navigation(NavigationType.STACK.name, *types)

fun <Params : Parcelable, Instance : Any> NavigationContext<Params>.stack(
    initial: Params,
    addInitial: Boolean = false,
    handleBackButton: Boolean = true,
    tag: String? = null,
    factory: (params: Params, context: NavigationContext<Params>) -> Instance,
) = stack(
    initialProvider = { listOf(initial) },
    addInitial = addInitial,
    handleBackButton = handleBackButton,
    tag = tag,
    factory,
)

fun <Params : Parcelable, Instance : Any> NavigationContext<Params>.stack(
    initialProvider: () -> List<Params>,
    addInitial: Boolean = false,
    handleBackButton: Boolean = true,
    tag: String? = null,
    factory: (params: Params, context: NavigationContext<Params>) -> Instance,
): Value<ChildStack<Params, Instance>> {
    val tag = tag ?: NavigationType.STACK.name
    val navigationHolder = navigation.provideHolder(tag) { pending ->
        val pendingStack: List<Params>? = pending?.toList()
        val initialScreens = when {
            pendingStack.isNullOrEmpty() -> initialProvider()
            addInitial -> initialProvider() + pendingStack
            else -> pendingStack
        }
        StackNavigationHolder(tag, initialScreens)
    }
    return children(
        navigationHolder = navigationHolder,
        handleBackButton = handleBackButton,
        tag = tag,
        stateMapper = { _, children ->
            @Suppress("UNCHECKED_CAST")
            val createdChildren = children as List<Child.Created<Params, Instance>>

            ChildStack(
                active = createdChildren.last(),
                backStack = createdChildren.dropLast(1),
            )
        },
        factory,
    )
}
