package com.example.feed

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.example.navigation.FeedScreenParams
import com.example.navigation.FeedTabScreenParams
import com.example.navigation.SearchScreenParams
import com.example.navigation.context.ScreenContext
import com.example.navigation.navigation.NavigationRegister
import com.example.navigation.transaction.transaction
import com.example.navigation.router.ScreenRegister
import com.example.navigation.screens.ScreenParams
import com.example.navigation.screens.ViewScreen

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
        transaction { open(SearchScreenParams) }
    }

}

fun registerFeedScreens(
    register: ScreenRegister,
    navigationRegister: NavigationRegister<ScreenParams>
) {
    register.registerFactory(FeedScreenParams::class) { context, type ->
        FeedScreen(context, type)
    }

    navigationRegister.registerStackNavigation(
        FeedTabScreenParams::class,
        FeedScreenParams::class,
        SearchScreenParams::class
    )
}
