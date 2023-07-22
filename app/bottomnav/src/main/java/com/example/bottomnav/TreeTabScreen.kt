package com.example.bottomnav

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.navigation.TreeScreenParams
import com.example.navigation.TreeTabScreenParams
import com.example.navigation.context.ScreenContext
import com.example.navigation.screens.ViewScreen
import com.example.navigation.stack.StackHostView
import com.example.navigation.stack.stack
import com.example.navigation.view.ForwardBackwardTransition

class TreeTabScreen(context: ScreenContext, params: TreeTabScreenParams): ViewScreen<TreeTabScreenParams>(context, params) {

    private val stack = stack(TreeScreenParams, true)

    override fun onCreateView(layoutInflater: LayoutInflater, parent: ViewGroup): View {
        return layoutInflater.inflate(R.layout.tree_tab_layout, parent, false)
    }

    override fun onViewCreated(view: View) {
        val host: StackHostView = view.findViewById(R.id.tree_tab_host)
        host.observe(stack, viewLifecycle, ForwardBackwardTransition)
    }
}