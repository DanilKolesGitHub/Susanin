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
import com.example.navigation.tree.Tree
import kotlin.reflect.KClass

class TreeScreen(context: ScreenContext, type: TreeScreenParams): ViewScreen<TreeScreenParams>(context, type) {

    val navTree = toUi(this.navigation.tree.root)

    private fun toUi(root: Tree.Node<KClass<out ScreenParams>>): UiNode {
        return UiNode(root.data.simpleName?.removeSuffix("ScreenParams")?: "wtf", root.children.values.map { toUi(it) })
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
    ): View {
        val layout = ComposeView(container.context)
        layout.setContent {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                TreeView(navTree)
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