package com.example.video

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.example.navigation.*
import com.example.navigation.context.ScreenContext
import com.example.navigation.navigation.NavigationRegister
import com.example.navigation.router.ScreenRegister
import com.example.navigation.screens.ScreenFactory
import com.example.navigation.screens.ScreenParams
import com.example.navigation.screens.ViewScreen
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
            navigate()
        }
    }

    private fun navigate() {
        transaction {
            inside(FeedTabScreenParams::class).open(SearchScreenParams)
        }
    }

}

fun registerVideoScreens(
    register: ScreenRegister,
    navigationRegister: NavigationRegister<ScreenParams>
) {
    register.registerFactory(VideoScreenParams::class, object : ScreenFactory<VideoScreenParams> {
        override fun create(screenType: VideoScreenParams, context: ScreenContext): VideoScreen {
            return VideoScreen(context, screenType)
        }
    })

    navigationRegister.registerStackNavigation(
        VideoTabScreenParams::class,
        VideoScreenParams::class,
        SearchScreenParams::class
    )
}
