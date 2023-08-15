package com.example.navigation.pages

import android.os.Parcelable
import com.arkivanov.decompose.router.pages.ChildPages
import com.arkivanov.decompose.value.Value
import com.example.navigation.NavigationType
import com.example.navigation.context.NavigationContext
import com.example.navigation.dispatcher.Type
import com.example.navigation.navigation.children
import com.example.navigation.register.HierarchyBuilder

fun <Params : Parcelable> HierarchyBuilder<Params>.pages(vararg types: Type<Params>) =
    navigation(NavigationType.PAGES.name, *types)

fun <Params : Parcelable, Instance : Any> NavigationContext<Params>.pages(
    initialPages: List<Params>,
    initialSelection: Int,
    handleBackButton: Boolean = true,
    closeBehaviour: CloseBehaviour = CloseBehaviour.Circle,
    backBehaviour: BackBehaviour = BackBehaviour.Circle,
    tag: String? = null,
    factory: (params: Params, context: NavigationContext<Params>) -> Instance,
) = pages(
    { initialPages },
    initialSelection,
    handleBackButton,
    closeBehaviour,
    backBehaviour,
    tag,
    factory,
)

fun <Params : Parcelable, Instance : Any> NavigationContext<Params>.pages(
    initialProvider: () -> List<Params>,
    initialSelection: Int,
    handleBackButton: Boolean = true,
    closeBehaviour: CloseBehaviour,
    backBehaviour: BackBehaviour,
    tag: String? = null,
    factory: (params: Params, context: NavigationContext<Params>) -> Instance,
): Value<ChildPages<Params, Instance>> {
    val tag = tag ?: NavigationType.PAGES.name
    val navigationHolder = navigation.provideHolder(tag) { pending ->
        val pages = initialProvider()
        val pendingSelected: Params? = pending?.lastOrNull()
        val selected = pendingSelected?.let { pages.indexOf(it) } ?: initialSelection
        PagesNavigationHolder(
            tag,
            pages,
            selected,
            closeBehaviour.navigationBehaviour,
            backBehaviour.navigationBehaviour,
        )
    }
    return children(
        navigationHolder = navigationHolder,
        handleBackButton = handleBackButton,
        tag = tag,
        stateMapper = { state, children ->
            ChildPages(
                items = children,
                selectedIndex = state.selected,
            )
        },
        factory = factory,
    )
}

enum class BackBehaviour {
    ToFirst,
    UntilFirst,
    Circle,
    ;

    internal val navigationBehaviour: PagesNavigation.BackBehaviour
        get() = when (this) {
            ToFirst -> PagesNavigation.BackBehaviour.ToFirst
            UntilFirst -> PagesNavigation.BackBehaviour.UntilFirst
            Circle -> PagesNavigation.BackBehaviour.Circle
        }
}

enum class CloseBehaviour {
    ToFirst,
    UntilFirst,
    Circle,
    ;

    internal val navigationBehaviour: PagesNavigation.CloseBehaviour
        get() = when (this) {
            ToFirst -> PagesNavigation.CloseBehaviour.ToFirst
            UntilFirst -> PagesNavigation.CloseBehaviour.UntilFirst
            Circle -> PagesNavigation.CloseBehaviour.Circle
        }
}
