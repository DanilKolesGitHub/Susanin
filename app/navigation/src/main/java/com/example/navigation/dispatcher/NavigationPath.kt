package com.example.navigation.dispatcher

internal class NavigationPath<P : Any> {

    private val steps = mutableListOf<Step<P>>()

    val types: List<Type<P>>
        get() = steps.map { it.type }

    val params: Map<Type<P>, P>
        get() = steps
            .filterIsInstance<CertainStep<P>>()
            .associateBy(CertainStep<P>::type, CertainStep<P>::param)

    fun add(param: P) {
        steps.add(CertainStep(param))
    }

    fun add(type: Type<P>) {
        steps.add(VagueStep(type))
    }

    private sealed interface Step<P : Any> {
        val type: Type<P>
    }

    private data class CertainStep<P : Any>(
        val param: P,
    ) : Step<P> {
        override val type: Type<P>
            get() = param::class
    }

    private data class VagueStep<P : Any>(
        override val type: Type<P>,
    ) : Step<P>
}
