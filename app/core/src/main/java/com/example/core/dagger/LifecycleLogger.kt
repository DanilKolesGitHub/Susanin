package com.example.core.dagger

import com.arkivanov.essenty.lifecycle.Lifecycle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class LifecycleLogger(val pref: String = "", val f: (String, String) -> Unit): Lifecycle.Callbacks {

    private val _state = MutableStateFlow<String>("");
    val state: StateFlow<String> = _state

    init {
        log("constructor")
    }

    override fun onCreate() {
         log("onCreate")
    }

    override fun onDestroy() {
         log("onDestroy")
    }

    override fun onPause() {
         log("onPause")
    }

    override fun onResume() {
         log("onResume")
    }

    override fun onStart() {
         log("onStart")
    }

    override fun onStop() {
         log("onStop")
    }

    private fun log(string: String){
        _state.tryEmit(string)
        f("LCDEB", "$pref $string")
    }
}