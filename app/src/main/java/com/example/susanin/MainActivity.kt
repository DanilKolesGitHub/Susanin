package com.example.susanin

import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.arkivanov.decompose.defaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import com.arkivanov.essenty.lifecycle.create
import com.arkivanov.essenty.lifecycle.destroy
import com.arkivanov.essenty.lifecycle.pause
import com.arkivanov.essenty.lifecycle.resume
import com.arkivanov.essenty.lifecycle.start
import com.arkivanov.essenty.lifecycle.stop
import com.example.bottomnav.registerTabScreens
import com.example.feed.registerFeedScreens
import com.example.miniplayer.registerPlayerScreens
import com.example.navigation.MainScreenParams
import com.example.navigation.context.defaultScreenContext
import com.example.navigation.register.ScreenRegisterImpl
import com.example.search.registerSearchScreens
import com.example.tree.registerTreeScreens
import com.example.video.registerVideoScreens

class MainActivity : AppCompatActivity() {

    val viewLifecycle: LifecycleRegistry = LifecycleRegistry()
    var restored = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val register = ScreenRegisterImpl()
        registerTabScreens(register)
        registerFeedScreens(register)
        registerVideoScreens(register)
        registerTreeScreens(register)
        registerSearchScreens(register)
        registerMainScreens(register)
        registerPlayerScreens(register)

        val mainScreen = MainScreen(
            register.defaultScreenContext(
                defaultComponentContext(),
                MainScreenParams
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