package com.example.navigation.stack

import android.os.Parcelable
import com.arkivanov.decompose.Child
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import com.example.navigation.NavigationType
import com.example.navigation.context.NavigationContext
import com.example.navigation.transaction.TransactionBuilder
import com.example.navigation.navigation.children

fun <Params : Parcelable, Instance : Any> NavigationContext<Params>.stack(
    initial: Params,
    addInitial: Boolean = false,
    handleBackButton: Boolean = true,
    tag: String = NavigationType.STACK.name,
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
    tag: String = NavigationType.STACK.name,
    factory: (params: Params, context: NavigationContext<Params>) -> Instance,
): Value<ChildStack<Params, Instance>> {
    val navigationHolder = navigation.provideHolder(tag) { pending ->
        val initialScreens = if (pending != null) {
            if (addInitial)
                initialProvider() + pending
            else
                listOf(pending)
        } else {
            initialProvider()
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
