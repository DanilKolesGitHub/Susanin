package com.example.navigation.transaction

import com.example.navigation.context.NavigationContext
import com.example.navigation.dispatcher.NavigationPath
import com.example.navigation.dispatcher.Type
import com.example.navigation.navigation.NavigationManager
import java.util.LinkedList

class TransactionBuilder<P : Any> {

    internal val transactions = LinkedList<Transaction<P>>()
    private var path = NavigationPath<P>()

    fun inside(type: Type<P>) = apply {
        path.add(type)
    }

    fun inside(params: P) = apply {
        path.add(params)
    }

    fun open(type: Type<P>) {
        path.add(type)
        addTransaction(Open(path))
    }

    fun open(params: P) {
        path.add(params)
        addTransaction(Open(path))
    }

    fun close(type: Type<P>) {
        path.add(type)
        addTransaction(Close(path))
    }

    fun close(params: P) {
        path.add(params)
        addTransaction(Close(path))
    }

    private fun addTransaction(transaction: Transaction<P>){
        transactions.add(transaction)
        path = NavigationPath()
    }
}

internal class Open<P : Any>(
    private val path: NavigationPath<P>,
) : Transaction<P> {

    override fun commit(navigationManager: NavigationManager<P>) {
        navigationManager.dispatcher.open(navigationManager, path)
    }
}

internal class Close<P : Any>(
    private val path: NavigationPath<P>,
) : Transaction<P> {

    override fun commit(navigationManager: NavigationManager<P>) {
        navigationManager.dispatcher.close(navigationManager, path)
    }
}

fun open(context: NavigationContext<String>) {
    context.transaction {
        val builder = inside("A")
        builder.open("sl")
        builder.close("sld;")
        builder.inside("ksk")
        inside("A").open("B"::class)
        open("C")
    }
    context.transaction {
        inside("ksks").open("sks")
    }
}