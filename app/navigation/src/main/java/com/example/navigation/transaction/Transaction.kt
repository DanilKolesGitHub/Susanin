package com.example.navigation.transaction

import android.os.Handler
import android.os.Looper
import com.example.navigation.context.NavigationContext
import com.example.navigation.navigation.NavigationManager

interface Transaction<P : Any> {
    fun commit(navigationManager: NavigationManager<P>)
}

private fun<P : Any>  commit(manager: NavigationManager<P>, transactions: List<Transaction<P>>){
    val handler = Handler(Looper.getMainLooper())
    transactions.forEach{
        handler.post { it.commit(manager) }
    }
}

fun<P : Any> NavigationManager<P>.transaction(builder: TransactionBuilder<P>.() -> Unit) {
    val transactionBuilder = TransactionBuilder<P>()
    transactionBuilder.builder()
    commit(this, transactionBuilder.transactions)
}

fun<P : Any> NavigationContext<P>.transaction(builder: TransactionBuilder<P>.() -> Unit) {
    this.navigation.transaction(builder)
}
