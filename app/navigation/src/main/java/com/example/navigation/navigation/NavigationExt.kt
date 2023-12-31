package com.example.navigation.navigation

import android.os.Parcelable
import com.arkivanov.decompose.Child
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import com.arkivanov.decompose.router.children.NavState
import com.arkivanov.decompose.router.children.children
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.arkivanov.essenty.instancekeeper.getOrCreate
import com.arkivanov.essenty.parcelable.ParcelableContainer
import com.arkivanov.essenty.parcelable.consumeRequired
import com.example.navigation.context.DefaultNavigationContext
import com.example.navigation.context.NavigationContext
import com.example.navigation.layer.Layer

internal fun <Params: Any, State: NavState<Params>, Instance: Any, Ui: Any> NavigationContext<Params>.children(
    navigationHolder: NavigationHolder<Params, State>,
    handleBackButton: Boolean,
    tag: String,
    layer: Layer?,
    stateMapper: (state: State, children: List<Child<Params, Instance>>) -> Ui,
    factory: (params: Params, context: NavigationContext<Params>) -> Instance,
    saveState: (state: State) -> ParcelableContainer?,
    restoreState: (container: ParcelableContainer) -> State?,
): Value<Ui> {
    val decomposeFactory = { params: Params, componentContext: ComponentContext ->
        componentContext.instanceKeeper.getOrCreate(params) {
                object : InstanceKeeper.Instance {
                    override fun onDestroy() {
                        navigation.removeChild(params)
                    }
                }
        }
        factory(
            params,
            DefaultNavigationContext(
                componentContext,
                navigation.provideChild(params)
            )
        )
    }
    navigation.beforeApplyState(tag, navigationHolder.state, null)
    return childContext(tag, layer?.layerLifecycle()).children(
        source = navigationHolder.navigation,
        key = tag,
        initialState = { navigationHolder.state },
        saveState = saveState,
        restoreState = restoreState,
        navTransformer = { state, event ->
            event.transformer(state).also { newState ->
                navigation.beforeApplyState(tag, newState, state)
            }
        },
        backTransformer = { state ->
            if (handleBackButton && navigationHolder.navigation.canBack(state)) {
                {
                    navigationHolder.navigation.back(state).also { newState ->
                        navigation.beforeApplyState(tag, newState, state)
                    }
                }
            } else
                null
        },
        onEventComplete = { event, newState, oldState -> event.onComplete(newState, oldState) },
        stateMapper = stateMapper,
        onStateChanged = { newState, oldState ->
            navigation.afterApplyState(tag, newState, oldState)
        },
        childFactory = decomposeFactory,
    )
}

internal inline fun <Params: Parcelable, reified State, Instance: Any, Ui: Any> NavigationContext<Params>.children(
    navigationHolder: NavigationHolder<Params, State>,
    handleBackButton: Boolean,
    tag: String,
    layer: Layer?,
    noinline stateMapper: (state: State, children: List<Child<Params, Instance>>) -> Ui,
    noinline factory: (params: Params, context: NavigationContext<Params>) -> Instance,
): Value<Ui> where State : NavState<Params>, State : Parcelable =
    children(
        navigationHolder = navigationHolder,
        handleBackButton = handleBackButton,
        tag = tag,
        layer = layer,
        stateMapper = stateMapper,
        factory = factory,
        saveState = { ParcelableContainer(it) },
        restoreState = { it.consumeRequired(State::class) },
    )


