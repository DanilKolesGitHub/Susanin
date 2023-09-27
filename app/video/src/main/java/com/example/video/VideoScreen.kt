package com.example.video

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.subscribe
import com.example.navigation.DialogScreenParams
import com.example.navigation.PlayerScreenParams
import com.example.navigation.SearchScreenParams
import com.example.navigation.VideoScreenParams
import com.example.navigation.VideoTabScreenParams
import com.example.navigation.context.ScreenContext
import com.example.navigation.factory.ScreenFactory
import com.example.navigation.register.ScreenRegister
import com.example.navigation.screens.ViewScreen
import com.example.navigation.stack.stack
import com.example.navigation.transaction.transaction

class VideoScreen(context: ScreenContext, screenType: VideoScreenParams): ViewScreen<VideoScreenParams>(context, screenType) {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
    ): View {
        return inflater.inflate(R.layout.video_layout, container, false)
    }

    override fun onViewCreated(view: View) {
        val image: ImageView = view.findViewById(R.id.search)
        image.setOnClickListener {
            transaction {
                open(DialogScreenParams(Color.BLUE))
            }
        }
        printlc(view.findViewById<TextView>(R.id.video_text), lifecycle)
        view.findViewById<View>(R.id.video_card).setOnClickListener {
            transaction {
                open(PlayerScreenParams)
            }
        }
    }

    private fun navigate() {
        transaction {
            open(PlayerScreenParams)
//            open(DialogScreenParams(Color.BLUE))
        }
    }

    private fun printlc(textView: TextView, lifecycle: Lifecycle){
        lifecycle.subscribe(
            onCreate = {textView.text = "OnCreate"},
            onStart  = {textView.text = "onStart"},
            onResume = {textView.text = "onResume"},
            onPause = {textView.text = "onPause"},
            onStop = {textView.text = "onStop"},
            onDestroy = {textView.text = "onDestroy"},
        )
    }

}

fun registerVideoScreens(
    register: ScreenRegister,
) {
    register.registerFactory(VideoScreenParams::class, object : ScreenFactory<VideoScreenParams> {
        override fun create(screenType: VideoScreenParams, context: ScreenContext): VideoScreen {
            return VideoScreen(context, screenType)
        }
    })

    register.registerNavigation(VideoTabScreenParams){
        stack(VideoScreenParams::class, SearchScreenParams::class)
    }
}
