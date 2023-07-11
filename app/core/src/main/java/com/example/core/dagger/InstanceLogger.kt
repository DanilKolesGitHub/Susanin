package com.example.core.dagger

import com.arkivanov.essenty.instancekeeper.InstanceKeeper

class InstanceLogger(val pref: String = "", val f: (String, String) -> Unit): InstanceKeeper.Instance {

    init{
        log("onCreate")
    }

    private fun log(string: String){
        f("INDEB", "$pref $string")
    }

    override fun onDestroy() {
        log("onDestroy")
    }
}