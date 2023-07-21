package com.example.navigation.pages

import com.arkivanov.decompose.router.pages.ChildPages
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.parcelable.ParcelableContainer
import com.arkivanov.essenty.parcelable.consumeRequired
import com.example.navigation.NavigationType
import com.example.navigation.context.ScreenContext
import com.example.navigation.navigation.childScreens
import com.example.navigation.screens.Screen
import com.example.navigation.screens.ScreenParams

fun ScreenContext.pages(
    initialPages: List<ScreenParams>,
    initialSelection: Int,
    handleBackButton: Boolean = true,
    closeBehaviour: CloseBehaviour = CloseBehaviour.Circle,
    backBehaviour: BackBehaviour = BackBehaviour.Circle,
    tag: String = NavigationType.PAGES.name,
) = pages(
    { initialPages },
    initialSelection,
    handleBackButton,
    closeBehaviour,
    backBehaviour,
    tag
)

fun ScreenContext.pages(
    initialPages: () -> List<ScreenParams>,
    initialSelection: Int,
    handleBackButton: Boolean = true,
    closeBehaviour: CloseBehaviour,
    backBehaviour: BackBehaviour,
    tag: String = NavigationType.PAGES.name,
): Value<ChildPages<ScreenParams, Screen<*>>> {
    val navigationHolder = node.provideNavigation(tag) { pending ->
        val pages = initialPages()
        val selected = pending?.lastOrNull()?.let {
            pages.indexOf(it)
        } ?: initialSelection
        PagesNavigationHolder(
            tag,
            pages,
            selected,
            closeBehaviour.navigationBehaviour,
            backBehaviour.navigationBehaviour,
        )
    }
    return childScreens(
        navigationHolder = navigationHolder,
        handleBackButton = handleBackButton,
        tag = tag,
        stateMapper = { state, children ->
            ChildPages(
                items = children,
                selectedIndex = state.selected,
            )
        },
        saveState = { ParcelableContainer(it) },
        restoreState = { it.consumeRequired(PagesHostState::class) }
    )
}

enum class BackBehaviour {
    Never,
    ToFirst,
    UntilFirst,
    Circle;

    internal val navigationBehaviour : PagesNavigation.BackBehaviour
    get() = when (this) {
            ToFirst -> PagesNavigation.BackBehaviour.ToFirst
            UntilFirst -> PagesNavigation.BackBehaviour.UntilFirst
            Circle -> PagesNavigation.BackBehaviour.Circle
            Never -> PagesNavigation.BackBehaviour.Never
    }
}

enum class CloseBehaviour {
    ToFirst,
    UntilFirst,
    Circle;

    internal val navigationBehaviour : PagesNavigation.CloseBehaviour
        get() = when (this) {
            ToFirst -> PagesNavigation.CloseBehaviour.ToFirst
            UntilFirst -> PagesNavigation.CloseBehaviour.UntilFirst
            Circle -> PagesNavigation.CloseBehaviour.Circle
        }
}