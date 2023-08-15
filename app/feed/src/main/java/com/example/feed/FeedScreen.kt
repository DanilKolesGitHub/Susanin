package com.example.feed

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.example.navigation.FeedScreenParams
import com.example.navigation.FeedTabScreenParams
import com.example.navigation.ResultScreenParams
import com.example.navigation.SearchScreenParams
import com.example.navigation.VideoTabScreenParams
import com.example.navigation.context.ScreenContext
import com.example.navigation.register.ScreenRegister
import com.example.navigation.screens.ViewScreen
import com.example.navigation.stack.stack
import com.example.navigation.transaction.transaction

class FeedScreen(context: ScreenContext, type: FeedScreenParams): ViewScreen<FeedScreenParams>(context, type) {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
    ): View {
        return inflater.inflate(R.layout.feed_layout, container, false)
    }

    override fun onViewCreated(view: View) {
        val image: ImageView = view.findViewById(R.id.search)
        image.setOnClickListener {
            navigate()
        }
    }

    private fun navigate() {
        transaction {  inside(VideoTabScreenParams::class).open(ResultScreenParams("kjdsn")) }
    }

}

fun registerFeedScreens(
    register: ScreenRegister,
) {
    register.registerFactory<FeedScreenParams> { context, type ->
        FeedScreen(type, context)
    }

    register.registerNavigation(FeedTabScreenParams){
        stack(FeedScreenParams::class, SearchScreenParams::class)
    }
}
