package com.example.susanin

import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.arkivanov.decompose.defaultComponentContext
import com.arkivanov.essenty.lifecycle.*
import com.example.bottomnav.registerTabScreens
import com.example.feed.registerFeedScreens
import com.example.navigation.MainScreenParams
import com.example.navigation.context.DefaultNavigationContext
import com.example.navigation.context.DefaultScreenContext
import com.example.navigation.navigation.NavigationDispatcher
import com.example.navigation.navigation.NavigationManager
import com.example.navigation.navigation.NavigationRegister
import com.example.navigation.router.Router
import com.example.navigation.router.ScreenRegister
import com.example.navigation.screens.ScreenParams
import com.example.search.registerSearchScreens
import com.example.tree.registerTreeScreens
import com.example.video.registerVideoScreens

class MainActivity : AppCompatActivity() {

    val viewLifecycle: LifecycleRegistry = LifecycleRegistry()
    var restored = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val register = ScreenRegister()
        val navigationRegister = NavigationRegister<ScreenParams>()
        registerTabScreens(register, navigationRegister)
        registerFeedScreens(register, navigationRegister)
        registerVideoScreens(register, navigationRegister)
        registerTreeScreens(register, navigationRegister)
        registerSearchScreens(register, navigationRegister)
        registerMainScreens(register, navigationRegister)

        val rootManager = NavigationManager(MainScreenParams, null, NavigationDispatcher(
            navigationRegister.navigation,
            navigationRegister.default,
        ))
        val router = Router(register)
        val mainScreen = MainScreen(
            DefaultScreenContext(
                DefaultNavigationContext(
                    defaultComponentContext(),
                    rootManager
                ),
                router,
            )
        )
        val view = mainScreen.createView(window.decorView as ViewGroup, mainScreen.lifecycle)
        setContentView(view)
        viewLifecycle.create()
        restored = savedInstanceState != null
    }

    override fun onStart() {
        super.onStart()
        Log.d("SLDEB", "onStart")
        if (!restored)
            viewLifecycle.start()
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        Log.d("SLDEB", "restore")
        if (restored)
            viewLifecycle.start()
    }

    override fun onResume() {
        super.onResume()
        viewLifecycle.resume()
    }

    override fun onPause() {
        super.onPause()
        viewLifecycle.pause()
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
        viewLifecycle.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewLifecycle.destroy()
    }
}