package com.example.navigation.transaction

import com.example.navigation.context.NavigationContext
import com.example.navigation.navigation.NavigationManager
import kotlin.reflect.KClass

class TransactionBuilder<P : Any> {

    private val stages: MutableList<TransactionStage<P>> = mutableListOf()

    fun open(param: P) = addStage {
        val path = Path<P>().apply {
            add(param)
        }
        it.open(path)
    }

    fun close(param: P) = addStage {
        val path = Path<P>().apply {
            add(param)
        }
        it.close(path)
    }

    fun inside(type: KClass<out P>) : PathBuilder<P> {
        val builder = PathBuilder<P>()
        builder.inside(type)
        return builder
    }

    fun inside(param: P) : PathBuilder<P> {
        val builder = PathBuilder<P>()
        builder.inside(param)
        return builder
    }

    fun PathBuilder<P>.open(param: P) {
        inside(param)
        val path = this.path
        addStage {
            it.open(path)
        }
    }

    fun PathBuilder<P>.close(param: P) {
        inside(param)
        val path = this.path
        addStage {
            it.close(path)
        }
    }

    fun PathBuilder<P>.open(type: KClass<out P>) {
        inside(type)
        val path = this.path
        addStage {
            it.open(path)
        }
    }

    fun PathBuilder<P>.close(type: KClass<out P>) {
        inside(type)
        val path = this.path
        addStage {
            it.close(path)
        }
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

class PathBuilder<P : Any> {
    internal val path = Path<P>()

    fun inside(param: P) {
        path.add(param)
    }

    fun inside(type: KClass<out P>) {
        path.add(type)
    }
}
