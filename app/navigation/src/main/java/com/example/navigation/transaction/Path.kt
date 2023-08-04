package com.example.navigation.transaction

import kotlin.reflect.KClass

internal class Path<P : Any> {

    private val steps = mutableListOf<Step<P>>()

    val types: List<KClass<out P>>
        get() = steps.map { it.type }

    val params: Map<KClass<out P>, P>
        get() = steps
            .filterIsInstance<CertainStep<P>>()
            .associateBy(CertainStep<P>::type, CertainStep<P>::param)

    fun add(param: P) {
        steps.add(CertainStep(param))
    }

    fun add(type: KClass<out P>) {
        steps.add(VagueStep(type))
    }
}

private sealed interface Step<P : Any> {
    val type: KClass<out P>
}

private data class CertainStep<P : Any>(
    val param: P
) : Step<P> {
    override val type: KClass<out P>
        get() = param::class
}

private data class VagueStep<P : Any>(
    override val type: KClass<out P>
) : Step<P>
