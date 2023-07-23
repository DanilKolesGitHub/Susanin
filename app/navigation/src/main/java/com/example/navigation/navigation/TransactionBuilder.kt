package com.example.navigation.navigation

import com.example.navigation.context.NavigationContext

class TransactionBuilder {

    private val stages: MutableList<TransactionStage> = mutableListOf()

    fun <P : Any> open(params: P, tag: String) = addStage {
        it.findHolder<P>(tag)?.navigation?.open(params)
    }

    fun <P : Any> close(params: P, tag: String) = addStage {
        it.findHolder<P>(tag)?.navigation?.close(params)
    }

    fun <P : Any> parentOpen(params: P, tag: String) = addStage {
        it.parent?.findHolder<P>(tag)?.navigation?.open(params)
    }

    fun <P : Any> parentClose(params: P, tag: String) = addStage {
        it.parent?.findHolder<P>(tag)?.navigation?.close(params)
    }

    private fun addStage(
        command : (navigationNode: NavigationNode) -> Unit
    ) : TransactionBuilder {
        stages.add(object : TransactionStage {
            override fun invoke(navigationNode: NavigationNode) {
                command.invoke(navigationNode)
            }
        })
        return this
    }

    fun commit(navigationNode: NavigationNode) {
        Transaction(navigationNode, stages).invoke()
    }
}

fun NavigationNode.transaction(builder: TransactionBuilder.() -> Unit) {
    val transactionBuilder = TransactionBuilder()
    transactionBuilder.builder()
    transactionBuilder.commit(this)
}

fun NavigationContext.transaction(builder: TransactionBuilder.() -> Unit) {
    val transactionBuilder = TransactionBuilder()
    transactionBuilder.builder()
    transactionBuilder.commit(this.navigation)
}