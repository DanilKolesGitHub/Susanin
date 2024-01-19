package com.example.susanin

import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.defaultComponentContext
import com.example.miniplayer.registerPlayerScreens
import com.example.navigation.MainScreenParams
import com.example.navigation.context.defaultScreenContext
import com.example.navigation.layer.coroutineScope
import com.example.navigation.register.ScreenRegisterImpl
import com.example.navigation.root.RootHostView
import com.example.navigation.root.RootNavigation
import com.example.navigation.root.RootState
import com.example.navigation.root.root
import com.example.navigation.view.ViewRender
import com.example.tree.registerTreeScreens
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RootScreen(val activity: AppCompatActivity): ComponentContext by activity.defaultComponentContext() {

    val navigation = RootNavigation()
    val root = root(
        { Initializer.isInitialized() },
        navigation,
        ::factory
    )

    init {
        if (!Initializer.isInitialized()) {
            lifecycle.coroutineScope(Dispatchers.IO).launch {
                Initializer.init()
                withContext(Dispatchers.Main) {
                    navigation.initialized()
                }
            }
        }
    }

    private fun factory(rootState: RootState, componentContext: ComponentContext): ViewRender {
        return when(rootState){
            RootState.Splash -> SplashScreen(componentContext)
            RootState.Content -> createMain(componentContext)
        }
    }

    private fun createMain(componentContext: ComponentContext): ViewRender {
        val register = ScreenRegisterImpl()
        registerTreeScreens(register)
        registerMainScreens(register)
        registerPlayerScreens(register)
        registerStackScreens(register)
        registerSlotScreens(register)
        registerDialogsScreens(register)
        registerSelectScreens(register)

        return MainScreen(
            register.defaultScreenContext(
                componentContext,
                MainScreenParams
            )
        )
    }

    fun createView(): View {
        return LayoutInflater.from(activity).inflate(R.layout.root_screen, null)
            .also { onViewCreated(it) }
    }

    private fun onViewCreated(view: View) {
        view.findViewById<RootHostView>(R.id.root_screen_host).observe(root, lifecycle)
    }


}
