package com.example.navigation.navigation

import com.arkivanov.decompose.Child
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.children.children
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import com.arkivanov.essenty.instancekeeper.getOrCreate
import com.arkivanov.essenty.parcelable.ParcelableContainer
import com.example.navigation.context.DefaultScreenContext
import com.example.navigation.context.ScreenContext
import com.example.navigation.screens.Screen
import com.example.navigation.screens.ScreenParams
import com.example.navigation.state.HostState

fun <State: HostState, Ui: Any> ScreenContext.childScreens(
    navigationHolder: NavigationHolder<State>,
    handleBackButton: Boolean,
    tag: String,
    stateMapper: (state: State, children: List<Child<ScreenParams, Screen<*>>>) -> Ui,
    saveState: (state: State) -> ParcelableContainer?,
    restoreState: (container: ParcelableContainer) -> State?,
): Value<Ui> {
    val decomposeFactory = { params: ScreenParams, componentContext: ComponentContext ->
        componentContext.instanceKeeper.getOrCreate(params) {
                object : InstanceKeeper.Instance {
                    override fun onDestroy() { node.removeChild(params) }
                }
        }
        node.router.screenFactory.create(
            params,
            DefaultScreenContext(
                componentContext,
                node.provideChild(params)
            )
        )
    }
    return children(
        source = navigationHolder.navigator,
        key = tag,
        initialState = { navigationHolder.state },
        navTransformer = { state, event -> event.transformer(state) },
        backTransformer = { if (handleBackButton) navigationHolder.navigator.back(it) else null },
        onEventComplete = { event, newState, oldState -> event.onComplete(newState, oldState) },
        stateMapper = stateMapper,
        onStateChanged = { newState, oldState -> navigationHolder.state = newState },
        childFactory = decomposeFactory,
        saveState = saveState,
        restoreState = restoreState,
    )
}


