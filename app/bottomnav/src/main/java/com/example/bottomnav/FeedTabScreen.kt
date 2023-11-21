package com.example.bottomnav

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.navigation.FeedScreenParams
import com.example.navigation.FeedTabScreenParams
import com.example.navigation.context.ScreenContext
import com.example.navigation.screens.ViewScreen
import com.example.navigation.screens.stack
import com.example.navigation.stack.StackHostView
import com.example.navigation.view.ForwardBackwardTransition
import com.example.navigation.view.UiParams

class FeedTabScreen(context: ScreenContext, params: FeedTabScreenParams): ViewScreen<FeedTabScreenParams>(context, params), UiParams {

    private val stack = stack(FeedScreenParams, true)

    override fun onCreateView(layoutInflater: LayoutInflater, parent: ViewGroup): View {
        return layoutInflater.inflate(R.layout.feed_tab_layout, parent, false)
    }

    override fun onViewCreated(view: View) {
        val host: StackHostView = view.findViewById(R.id.feed_tab_host)
        host.observe(stack, viewLifecycle, ForwardBackwardTransition)
    }
}