package com.example.navigation.screens

import com.example.navigation.NavigationType
import com.example.navigation.context.DefaultScreenContext
import com.example.navigation.context.NavigationContext
import com.example.navigation.context.ScreenContext
import com.example.navigation.dialogs.dialogs
import com.example.navigation.layer.Layer
import com.example.navigation.pages.BackBehaviour
import com.example.navigation.pages.CloseBehaviour
import com.example.navigation.pages.pages
import com.example.navigation.slot.slot
import com.example.navigation.stack.stack

private fun ScreenContext.defaultFactory(
    screenParams: ScreenParams,
    context: NavigationContext<ScreenParams>
) = extensions.factory.create(screenParams, DefaultScreenContext(context, extensions))

fun ScreenContext.pages(
    initialPages: List<ScreenParams>,
    initialSelection: Int,
    handleBackButton: Boolean = true,
    closeBehaviour: CloseBehaviour = CloseBehaviour.Circle,
    backBehaviour: BackBehaviour = BackBehaviour.Circle,
    tag: String = NavigationType.PAGES.name,
    layer: Layer? = null,
) = pages(
    initialPages = initialPages,
    initialSelection = initialSelection,
    handleBackButton = handleBackButton,
    closeBehaviour = closeBehaviour,
    backBehaviour = backBehaviour,
    tag = tag,
    layer = layer,
    factory = ::defaultFactory
)

fun ScreenContext.pages(
    initialProvider: () -> List<ScreenParams>,
    initialSelection: Int,
    handleBackButton: Boolean = true,
    closeBehaviour: CloseBehaviour = CloseBehaviour.Circle,
    backBehaviour: BackBehaviour = BackBehaviour.Circle,
    tag: String = NavigationType.PAGES.name,
    layer: Layer? = null,
) = pages(
    initialProvider = initialProvider,
    initialSelection = initialSelection,
    handleBackButton = handleBackButton,
    closeBehaviour = closeBehaviour,
    backBehaviour = backBehaviour,
    tag = tag,
    layer = layer,
    factory = ::defaultFactory
)

fun ScreenContext.slot(
    initialSlot: ScreenParams? = null,
    handleBackButton: Boolean = true,
    tag: String = NavigationType.SLOT.name,
    layer: Layer? = null,
) = slot(
    initialSlot = initialSlot,
    handleBackButton = handleBackButton,
    tag = tag,
    layer = layer,
    factory = ::defaultFactory
)

fun ScreenContext.slot(
    initialProvider: () -> ScreenParams? = { null },
    handleBackButton: Boolean = true,
    tag: String = NavigationType.SLOT.name,
    layer: Layer? = null,
) = slot(
    initialProvider = initialProvider,
    handleBackButton = handleBackButton,
    tag = tag,
    layer = layer,
    factory = ::defaultFactory
)

fun ScreenContext.stack(
    initialScreen: ScreenParams,
    addInitial: Boolean = false,
    handleBackButton: Boolean = true,
    tag: String = NavigationType.STACK.name,
    layer: Layer? = null,
) = this.stack(
    initial = initialScreen,
    addInitial = addInitial,
    handleBackButton = handleBackButton,
    tag = tag,
    layer = layer,
    factory = ::defaultFactory,
)

fun ScreenContext.stack(
    initialProvider: () -> List<ScreenParams>,
    addInitial: Boolean = false,
    handleBackButton: Boolean = true,
    tag: String = NavigationType.STACK.name,
    layer: Layer? = null,
) = this.stack(
    initialProvider = initialProvider,
    addInitial = addInitial,
    handleBackButton = handleBackButton,
    tag = tag,
    layer = layer,
    factory = ::defaultFactory,
)

fun ScreenContext.dialogs(
    initialScreen: ScreenParams?,
    handleBackButton: Boolean = true,
    tag: String = NavigationType.DIALOGS.name,
    layer: Layer? = null,
) = this.dialogs(
    initial = initialScreen,
    handleBackButton = handleBackButton,
    tag = tag,
    layer = layer,
    factory = ::defaultFactory,
)

fun ScreenContext.dialogs(
    initialProvider: () -> List<ScreenParams>,
    handleBackButton: Boolean = true,
    tag: String = NavigationType.DIALOGS.name,
    layer: Layer? = null,
) = this.dialogs(
    initialProvider = initialProvider,
    handleBackButton = handleBackButton,
    tag = tag,
    layer = layer,
    factory = ::defaultFactory,
)