package com.example.navigation.layer

import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.LifecycleOwner
import com.arkivanov.essenty.lifecycle.doOnDestroy
import com.example.navigation.context.NavigationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class Layer internal constructor(
    internal val context: NavigationContext<*>,
    private var lockFlow: StateFlow<Boolean>
) {

    private val scope = context.lifecycle.coroutineScope(Dispatchers.Main.immediate)

    fun layer(flow: StateFlow<Boolean>): Layer {
        lockFlow = combineState(lockFlow, flow, scope) { l1, l2 -> l1 || l2 }
        return this
    }

    fun layerLifecycle(): Lifecycle {
        val layerLifecycle = LayerLifecycle(lockFlow.value)
        scope.launch {
            lockFlow.collectLatest { lock ->
                if (lock) layerLifecycle.lock() else layerLifecycle.release()
            }
        }
        return layerLifecycle
    }
}

fun CoroutineScope(
    context: CoroutineContext,
    lifecycle: Lifecycle,
): CoroutineScope {
    val scope = CoroutineScope(context)
    lifecycle.doOnDestroy(scope::cancel)
    return scope
}

fun LifecycleOwner.coroutineScope(context: CoroutineContext): CoroutineScope =
    CoroutineScope(context, lifecycle)

fun Lifecycle.coroutineScope(context: CoroutineContext): CoroutineScope =
    CoroutineScope(context, this)

fun <T1, T2, R> combineState(
    flow1: StateFlow<T1>,
    flow2: StateFlow<T2>,
    scope: CoroutineScope,
    sharingStarted: SharingStarted = SharingStarted.Eagerly,
    transform: (T1, T2) -> R
): StateFlow<R> = combine(flow1, flow2) {
        o1, o2 -> transform.invoke(o1, o2)
}.stateIn(scope, sharingStarted, transform.invoke(flow1.value, flow2.value))