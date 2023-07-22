package com.example.bottomnav

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.navigation.VideoScreenParams
import com.example.navigation.VideoTabScreenParams
import com.example.navigation.context.ScreenContext
import com.example.navigation.screens.ViewScreen
import com.example.navigation.stack.StackHostView
import com.example.navigation.stack.stack
import com.example.navigation.view.ForwardBackwardTransition

class VideoTabScreen(context: ScreenContext, params: VideoTabScreenParams): ViewScreen<VideoTabScreenParams>(context, params) {

    private val stack = stack(VideoScreenParams, true)

    override fun onCreateView(layoutInflater: LayoutInflater, parent: ViewGroup): View {
        return layoutInflater.inflate(R.layout.video_tab_layout, parent, false)
    }

    override fun onViewCreated(view: View) {
        val host: StackHostView = view.findViewById(R.id.video_tab_host)
        host.observe(stack, viewLifecycle, ForwardBackwardTransition)
    }}