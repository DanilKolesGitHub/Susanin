package com.example.navigation.navigation

import android.os.Handler
import android.os.Looper

class Transaction(
    private val navigationNode: NavigationNode,
    private val stages: List<TransactionStage>
) {

    fun invoke() {
        Handler(Looper.getMainLooper()).post {
            stages.forEach { it.invoke(navigationNode) }
        }
    }
}

interface TransactionStage {
    fun invoke(navigationNode: NavigationNode)
}