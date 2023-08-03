package com.example.navigation.transaction

import android.os.Handler
import android.os.Looper
import com.example.navigation.navigation.NavigationManager

class Transaction<P : Any>(
    private val navigationManager: NavigationManager<P>,
    private val stages: List<TransactionStage<P>>
) {

    fun invoke() {
        Handler(Looper.getMainLooper()).post {
            stages.forEach { it.invoke(navigationManager) }
        }
    }
}

interface TransactionStage<P : Any> {
    fun invoke(navigationManager: NavigationManager<P>)
}