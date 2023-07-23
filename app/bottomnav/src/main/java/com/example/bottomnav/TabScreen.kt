package com.example.bottomnav

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.arkivanov.decompose.value.observe
import com.example.navigation.FeedTabScreenParams
import com.example.navigation.TabScreenParams
import com.example.navigation.TreeTabScreenParams
import com.example.navigation.VideoTabScreenParams
import com.example.navigation.context.ScreenContext
import com.example.navigation.navigation.transaction
import com.example.navigation.pages.PagesHostView
import com.example.navigation.pages.openPages
import com.example.navigation.router.ScreenRegister
import com.example.navigation.screens.ScreenFactory
import com.example.navigation.screens.ScreenParams
import com.example.navigation.screens.ViewScreen
import com.example.navigation.screens.pages
import com.example.navigation.view.ForwardBackwardTransition

class TabScreen(context: ScreenContext, type: TabScreenParams): ViewScreen<TabScreenParams>(context, type) {

    private val screens = listOf(FeedTabScreenParams, VideoTabScreenParams, TreeTabScreenParams)
    private val menuItems = listOf(R.id.feed_item, R.id.video_item, R.id.tree_item)

    private val pages = pages(screens, 0)

    private fun findMenuId(active: ScreenParams): Int {
        return when(active) {
            FeedTabScreenParams -> R.id.feed_item
            VideoTabScreenParams -> R.id.video_item
            TreeTabScreenParams -> R.id.tree_item
            else -> throw IllegalStateException()
        }
    }

    private fun findScreenParam(id: Int): ScreenParams {
        return when(id) {
            R.id.feed_item -> FeedTabScreenParams
            R.id.video_item -> VideoTabScreenParams
            R.id.tree_item -> TreeTabScreenParams
            else -> throw IllegalStateException()
        }
    }

    override fun onCreateView(layoutInflater: LayoutInflater, parent: ViewGroup): View {
        return layoutInflater.inflate(R.layout.bottom_nav_layout, parent, false)
    }

    override fun onViewCreated(view: View) {
        val host: PagesHostView = view.findViewById(R.id.tab_page_host)
        host.observe(pages, viewLifecycle, ForwardBackwardTransition)
        pages.observe(viewLifecycle) {
            val newId = findMenuId(it.items[it.selectedIndex].configuration)
            menuItems.forEach { item ->
                view.findViewById<ImageView>(item).isSelected = item ==  newId
            }
        }
        menuItems.forEach { item ->
            view.findViewById<ImageView>(item).setOnClickListener {
                menuItems.forEach { item -> view.findViewById<ImageView>(item).isSelected = false }
                it.isSelected = true
                navigate(findScreenParam(item))
            }
        }
    }

    private fun navigate(screen: ScreenParams) {
        transaction {
            openPages(screen)
        }
    }
}

fun registerTabScreens(register: ScreenRegister) {
    register.registerFactory(
        TabScreenParams::class,
        object : ScreenFactory<TabScreenParams> {
            override fun create(screenType: TabScreenParams, context: ScreenContext): TabScreen {
                return TabScreen(context, screenType)
            }
        }
    )

    register.registerFactory(
        FeedTabScreenParams::class,
        object : ScreenFactory<FeedTabScreenParams> {
            override fun create(screenType: FeedTabScreenParams, context: ScreenContext): FeedTabScreen {
                return FeedTabScreen(context, screenType)
            }
        }
    )

    register.registerFactory(
        VideoTabScreenParams::class,
        object : ScreenFactory<VideoTabScreenParams> {
            override fun create(screenType: VideoTabScreenParams, context: ScreenContext): VideoTabScreen {
                return VideoTabScreen(context, screenType)
            }
        }
    )

    register.registerFactory(
        TreeTabScreenParams::class,
        object : ScreenFactory<TreeTabScreenParams> {
            override fun create(screenType: TreeTabScreenParams, context: ScreenContext): TreeTabScreen {
                return TreeTabScreen(context, screenType)
            }
        }
    )
}