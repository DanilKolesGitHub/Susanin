package com.example.navigation.dialogs

import android.os.Parcelable
import com.arkivanov.decompose.Child
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.subscribe
import com.example.navigation.NavigationType
import com.example.navigation.context.NavigationContext
import com.example.navigation.dispatcher.Type
import com.example.navigation.layer.Layer
import com.example.navigation.listener.StateListener
import com.example.navigation.navigation.children
import com.example.navigation.register.HierarchyBuilder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

fun <Params : Parcelable> NavigationContext<Params>.dialogsLayer(
    tag: String = NavigationType.DIALOGS.name,
): Layer {
    return Layer(this, dialogsFlow(tag))
}

fun Layer.dialogsLayer(
    tag: String = NavigationType.DIALOGS.name,
): Layer {
    layer(context.dialogsFlow(tag))
    return this
}

fun  NavigationContext<*>.dialogsFlow(
    tag: String = NavigationType.DIALOGS.name,
): StateFlow<Boolean> {
    val flow = MutableStateFlow(false)
    val listener = object : StateListener<DialogsHostState<*>> {
        override fun onBeforeApply(
            newState: DialogsHostState<*>,
            oldState: DialogsHostState<*>?
        ) {
            if (newState.dialogs.isNotEmpty()) {
                flow.update { true }
            }
        }

        override fun onAfterApply(
            newState: DialogsHostState<*>,
            oldState: DialogsHostState<*>?
        ) {
            if (newState.dialogs.isEmpty()) {
                flow.update { false }
            }
        }
    }
    lifecycle.subscribe(
        onCreate = {
            navigation.addStateListener(tag, listener)
        },
        onDestroy = {
            navigation.removeStateListener(tag, listener)
        }
    )
    return flow
}

fun <Params : Parcelable> HierarchyBuilder<Params>.dialogs(vararg types: Type<Params>) =
    navigation(NavigationType.DIALOGS.name, *types)

fun <Params : Parcelable, Instance : Any> NavigationContext<Params>.dialogs(
    initial: Params?,
    handleBackButton: Boolean = true,
    tag: String? = null,
    layer: Layer? = null,
    factory: (params: Params, context: NavigationContext<Params>) -> Instance,
) = dialogs(
    initialProvider = { initial?.let{ listOf(it) } ?: emptyList() },
    handleBackButton = handleBackButton,
    tag = tag,
    layer = layer,
    factory,
)

fun <Params : Parcelable, Instance : Any> NavigationContext<Params>.dialogs(
    initialProvider: () -> List<Params>,
    handleBackButton: Boolean = true,
    tag: String? = null,
    layer: Layer? = null,
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
        layer = layer,
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
