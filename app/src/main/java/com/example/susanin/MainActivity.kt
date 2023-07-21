package com.example.susanin

import android.os.Bundle
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.arkivanov.decompose.defaultComponentContext
import com.example.bottomnav.registerTabScreens
import com.example.feed.registerFeedScreens
import com.example.navigation.MainScreenParams
import com.example.navigation.context.DefaultScreenContext
import com.example.navigation.navigation.NavigationNode
import com.example.navigation.router.Router
import com.example.navigation.router.ScreenRegister
import com.example.search.registerSearchScreens
import com.example.tree.registerTreeScreens
import com.example.video.registerVideoScreens

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val register = ScreenRegister()
        registerTabScreens(register)
        registerFeedScreens(register)
        registerVideoScreens(register)
        registerTreeScreens(register)
        registerSearchScreens(register)
        registerMainScreens(register)

        val router = Router(register)
        val rootNode = NavigationNode(MainScreenParams, null, router)
        router.initRoot(rootNode)
        val mainScreen = MainScreen(
            DefaultScreenContext(
                defaultComponentContext(),
                rootNode,
            )
        )
        val view = mainScreen.onCreateView(layoutInflater, window.decorView as ViewGroup)
        setContentView(view)
        mainScreen.onViewCreated(view)
    }

}