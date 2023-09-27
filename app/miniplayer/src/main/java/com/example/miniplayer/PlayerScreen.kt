package com.example.miniplayer

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import com.arkivanov.essenty.backhandler.BackCallback
import com.example.navigation.MainScreenParams
import com.example.navigation.PlayerScreenParams
import com.example.navigation.context.ScreenContext
import com.example.navigation.factory.ScreenFactory
import com.example.navigation.register.ScreenRegister
import com.example.navigation.screens.ViewScreen
import com.example.navigation.transaction.transaction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class PlayerScreen(context: ScreenContext, screenType: PlayerScreenParams)
    : ViewScreen<PlayerScreenParams>(context, screenType) {

    init {
        backHandler.register(object : BackCallback() {
            override fun onBack() {
                Log.d("PLDEB", "onBack")
            }
        })
    }

    override fun onCreate() {
        Log.d("PLDEB", "onCreate")
        PlayerState.flow.update { true }
    }

    override fun onDestroy() {
        Log.d("PLDEB", "onDestroy")
        PlayerState.flow.update { false }
    }

    fun expand() {
        Log.d("PLDEB", "expand")
        PlayerState.flow.update { true }
    }

    fun collapse() {
        Log.d("PLDEB", "collapse")
        PlayerState.flow.update { false }
    }

    fun close() {
        transaction { close(params) }
    }


    override fun onCreateView(layoutInflater: LayoutInflater, parent: ViewGroup): View {
        val layout = ComposeView(parent.context)
        layout.setContent { PlayerView(this) }
        return layout
    }

}

fun registerPlayerScreens(register: ScreenRegister) {
    register.registerFactory(PlayerScreenParams::class,
        object : ScreenFactory<PlayerScreenParams> {
        override fun create(screenType: PlayerScreenParams, context: ScreenContext): PlayerScreen {
            return PlayerScreen(context, screenType)
        }
    })
    register.registerNavigation(MainScreenParams::class){
        navigation("miniplayer", PlayerScreenParams::class)
    }
}

object PlayerState {
    val flow = MutableStateFlow(false)
}