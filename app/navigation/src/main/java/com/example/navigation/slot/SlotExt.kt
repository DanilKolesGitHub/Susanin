package com.example.navigation.slot

import android.os.Parcelable
import com.arkivanov.decompose.Child
import com.arkivanov.decompose.router.slot.ChildSlot
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

fun <Params : Parcelable> NavigationContext<Params>.slotLayer(
    tag: String = NavigationType.SLOT.name,
): Layer {
    return Layer(this, slotFlow(tag))
}

fun Layer.slotLayer(
    tag: String = NavigationType.SLOT.name,
): Layer {
    layer(context.slotFlow(tag))
    return this
}

fun NavigationContext<*>.slotFlow(
    tag: String = NavigationType.SLOT.name,
): StateFlow<Boolean> {
    val lockValue = MutableStateFlow(false)
    val listener = object : StateListener<SlotHostState<*>> {
        override fun onBeforeApply(
            newState: SlotHostState<*>,
            oldState: SlotHostState<*>?
        ) {
            if (newState.slot != null) {
                lockValue.update { true }
            }
        }

        override fun onAfterApply(
            newState: SlotHostState<*>,
            oldState: SlotHostState<*>?
        ) {
            if (newState.slot == null) {
                lockValue.update { false }
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
    return lockValue
}


fun <Params : Parcelable> HierarchyBuilder<Params>.slot(vararg types: Type<Params>) =
    navigation(NavigationType.SLOT.name, *types)

fun <Params : Parcelable, Instance : Any> NavigationContext<Params>.slot(
    initialSlot: Params? = null,
    handleBackButton: Boolean = true,
    tag: String? = null,
    layer: Layer? = null,
    factory: (params: Params, context: NavigationContext<Params>) -> Instance,
) = slot(
    initialProvider = { initialSlot },
    handleBackButton = handleBackButton,
    tag = tag,
    layer = layer,
    factory = factory,
)

fun <Params : Parcelable, Instance : Any> NavigationContext<Params>.slot(
    initialProvider: () -> Params?,
    handleBackButton: Boolean = true,
    tag: String? = null,
    layer: Layer? = null,
    factory: (params: Params, context: NavigationContext<Params>) -> Instance,
): Value<ChildSlot<Params, Instance>> {
    val tag = tag ?: NavigationType.SLOT.name
    val navigationHolder = navigation.provideHolder(tag) { pending ->
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
        layer = layer,
        stateMapper = { _, children ->
            @Suppress("UNCHECKED_CAST")
            val createdChild = children.firstOrNull() as? Child.Created?
            ChildSlot(createdChild)
        },
        factory,
    )
}
