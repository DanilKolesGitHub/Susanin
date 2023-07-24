package com.example.navigation.navigation

import android.os.Handler
import android.os.Looper

class Transaction(
    private val navigationManager: NavigationManager,
    private val stages: List<TransactionStage>
) {

    fun invoke() {
        Handler(Looper.getMainLooper()).post {
            stages.forEach { it.invoke(navigationManager) }
        }
    }
}

interface TransactionStage {
    fun invoke(navigationManager: NavigationManager)
}