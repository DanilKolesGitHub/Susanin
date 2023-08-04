package com.example.tree

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import com.example.navigation.*
import com.example.navigation.context.ScreenContext
import com.example.navigation.navigation.NavigationRegister
import com.example.navigation.router.ScreenRegister
import com.example.navigation.screens.ScreenFactory
import com.example.navigation.screens.ScreenParams
import com.example.navigation.screens.ViewScreen
import com.example.navigation.transaction.PathBuilder
import com.example.navigation.transaction.transaction
import com.example.navigation.tree.Tree
import kotlin.reflect.KClass

class TreeScreen(context: ScreenContext, type: TreeScreenParams): ViewScreen<TreeScreenParams>(context, type) {

    val navTree = toUi(this.navigation.tree.root, listOf())

    private fun toUi(root: Tree.Node<KClass<out ScreenParams>>, parents: List<KClass<out ScreenParams>>): UiNode {
        val newParents = parents + root.data
        return UiNode(
            root.data,
            root.children.values.map { toUi(it, newParents) },
            parents
        )
    }

    fun openNode(uiNode: UiNode) {
        transaction {
            val builder = PathBuilder<ScreenParams>()
            uiNode.parents.forEach {
                builder.inside(it)
            }
            val params = toParams(uiNode.type)
            if (params == null)
                builder.open(uiNode.type)
            else
                builder.open(params)
        }
    }

    private fun toParams(type: KClass<out ScreenParams>): ScreenParams?{
        return when (type) {
            ResultScreenParams::class -> ResultScreenParams("hello")
            DialogScreenParams::class -> DialogScreenParams("hello")
            else -> null
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
    ): View {
        val layout = ComposeView(container.context)
        layout.setContent {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                TreeView(navTree, ::openNode)
            }
        }
        return layout
    }
}

fun registerTreeScreens(
    register: ScreenRegister,
    navigationRegister: NavigationRegister<ScreenParams>
) {
    register.registerFactory(TreeScreenParams::class, object : ScreenFactory<TreeScreenParams> {
        override fun create(screenType: TreeScreenParams, context: ScreenContext): TreeScreen {
            return TreeScreen(context, screenType)
        }
    })

    navigationRegister.registerStackNavigation(
        TreeTabScreenParams::class,
        TreeScreenParams::class
    )
}