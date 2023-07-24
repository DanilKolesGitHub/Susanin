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
        command : (navigationManager: NavigationManager) -> Unit
    ) : TransactionBuilder {
        stages.add(object : TransactionStage {
            override fun invoke(navigationManager: NavigationManager) {
                command.invoke(navigationManager)
            }
        })
        return this
    }

    fun commit(navigationManager: NavigationManager) {
        Transaction(navigationManager, stages).invoke()
    }
}

fun NavigationManager.transaction(builder: TransactionBuilder.() -> Unit) {
    val transactionBuilder = TransactionBuilder()
    transactionBuilder.builder()
    transactionBuilder.commit(this)
}

fun NavigationContext.transaction(builder: TransactionBuilder.() -> Unit) {
    val transactionBuilder = TransactionBuilder()
    transactionBuilder.builder()
    transactionBuilder.commit(this.navigation)
}