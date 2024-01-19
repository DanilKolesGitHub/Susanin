package com.example.tree

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import com.example.navigation.PlayerScreenParams
import com.example.navigation.SelectScreenParams
import com.example.navigation.TestScreenParams
import com.example.navigation.TreeScreenParams
import com.example.navigation.context.ScreenContext
import com.example.navigation.factory.ScreenFactory
import com.example.navigation.pages.pages
import com.example.navigation.register.ScreenRegister
import com.example.navigation.screens.ScreenParams
import com.example.navigation.screens.ViewScreen
import com.example.navigation.transaction.transaction
import com.example.navigation.tree.Tree
import kotlin.reflect.KClass

class TreeScreen(context: ScreenContext, type: TreeScreenParams): ViewScreen<TreeScreenParams>(context, type) {

    val navTree = toUi(this.navigation.dispatcher.tree.root, listOf())

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
            uiNode.parents.forEach {
                inside(it)
            }
            val params = toParams(uiNode.type)
            if (params == null)
                open(uiNode.type)
            else
                open(params)
        }
    }

    private fun toParams(type: KClass<out ScreenParams>): ScreenParams?{
        return when (type) {
            PlayerScreenParams::class -> PlayerScreenParams
            TestScreenParams::class -> TestScreenParams(0)
            else -> null
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
    ): View {
        val layout = ComposeView(container.context).apply {
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
        layout.setContent {
            LaunchedEffect(Unit) {
                Log.d("COMDEB", "launch effect")
            }
            Column(Modifier.verticalScroll(rememberScrollState())) {
                TreeView(navTree, ::openNode)
            }
        }
        return layout
    }
}

fun registerTreeScreens(
    register: ScreenRegister,
) {
    register.registerFactory(TreeScreenParams::class, object : ScreenFactory<TreeScreenParams> {
        override fun create(screenType: TreeScreenParams, context: ScreenContext): TreeScreen {
            return TreeScreen(context, screenType)
        }
    })
    register.registerNavigation(SelectScreenParams) {
        pages(TreeScreenParams::class)
    }
}