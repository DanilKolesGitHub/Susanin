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

    fun addStep(param: P) {
        steps.add(CertainStep(param))
    }

    fun addStep(type: KClass<P>) {
        steps.add(VagueStep(type))
    }

    fun foreachType(action: (KClass<out P>) -> Unit) {
        for (step in steps)
            action(step.type)
    }

    fun foreach(
        default: Map<KClass<out P>, P>,
        action: (P) -> Unit
    ) {
        for (step in steps) {
            when (step) {
                is CertainStep -> action(step.param)
                is VagueStep -> action(default[step.type]!!)
            }
        }
    }

    operator fun get(type: KClass<P>): P? {
        var result: P? = null
        for (step in steps) {
            if (step.type == type && step is CertainStep) {
                result = step.param
            }
        }
        return result
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
    override val type: KClass<P>
) : Step<P>
