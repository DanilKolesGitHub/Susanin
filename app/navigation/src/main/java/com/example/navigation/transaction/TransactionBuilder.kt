package com.example.navigation.transaction

import com.example.navigation.context.NavigationContext
import com.example.navigation.navigation.NavigationManager

class TransactionBuilder<P : Any> {

    private val stages: MutableList<TransactionStage<P>> = mutableListOf()

    fun open(params: P) = addStage {
        val path = Path<P>().apply {
            addStep(params)
        }
        it.open(path)
    }

    private fun addStage(
        command: (navigationManager: NavigationManager<P>) -> Unit
    ): TransactionBuilder<P> {
        stages.add(object : TransactionStage<P> {
            override fun invoke(navigationManager: NavigationManager<P>) {
                command.invoke(navigationManager)
            }
        })
        return this
    }

    fun commit(navigationManager: NavigationManager<P>) {
        Transaction<P>(navigationManager, stages).invoke()
    }
}

fun <P : Any> NavigationContext<P>.transaction(builder: TransactionBuilder<P>.() -> Unit) {
    val transactionBuilder = TransactionBuilder<P>()
    transactionBuilder.builder()
    transactionBuilder.commit(this.navigation)
}