package com.example.susanin

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import com.arkivanov.essenty.lifecycle.doOnPause
import com.arkivanov.essenty.lifecycle.doOnResume
import com.arkivanov.essenty.lifecycle.subscribe
import com.example.navigation.OverlayScreenParams
import com.example.navigation.context.ScreenContext
import com.example.navigation.screens.ViewScreen
import com.example.navigation.transaction.transaction

class OverlayScreen(context: ScreenContext, type: OverlayScreenParams): ViewScreen<OverlayScreenParams>(context, type) {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
    ): View {
        return inflater.inflate(R.layout.overlay_screen, container, false).apply {
            layoutParams = FrameLayout.LayoutParams(layoutParams).apply {
                height = 1000 + params.id*100
                gravity = Gravity.BOTTOM
            }
        }
    }

    var lc: TextView? = null

    override fun onViewCreated(container: View) {
        val result = container.findViewById<Button>(R.id.overlay_button)
        val reverse = container.findViewById<Button>(R.id.overlay_reverse)
        val et = container.findViewById<EditText>(R.id.overlay_input)
        lc = container.findViewById(R.id.lc_txt)
        printlc()

//        result.visibility = GONE
//        reverse.visibility = GONE
//        et.visibility = GONE
        result.visibility = VISIBLE
        reverse.visibility = VISIBLE
        et.visibility = VISIBLE

        result.setOnClickListener {
            transaction {
                open(OverlayScreenParams(true, params.id+1))
            }
        }

        reverse.setOnClickListener {
            navigation.parent!!.update { it.reversed() }
        }

        viewLifecycle.doOnResume {
            result.visibility = VISIBLE
            reverse.visibility = VISIBLE
            et.visibility = VISIBLE
        }
        viewLifecycle.doOnPause {
            result.visibility = GONE
            reverse.visibility = GONE
            et.visibility = GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        lc = null
    }

    private var sl: String = "init"
        get() = field
        set(value: String) {
            field = value
            updateText()
        }

    private var vl: String = "init"
        get() = field
        set(value: String) {
            field = value
            updateText()
        }

    private fun printlc(){
        lifecycle.subscribe(
            onCreate =  { sl = "CREATED"},
            onStart  =  { sl = "STARTED"},
            onResume =  { sl = "RESUMED"},
            onPause =   { sl = "STARTED"},
            onStop =    { sl = "CREATED"},
            onDestroy = { sl = "DESTROYED"},
        )
        viewLifecycle.subscribe(
            onCreate =  { vl = "CREATED"},
            onStart  =  { vl = "STARTED"},
            onResume =  { vl = "RESUMED"},
            onPause =   { vl = "STARTED"},
            onStop =    { vl = "CREATED"},
            onDestroy = { vl = "DESTROYED"},
        )
    }

    private fun updateText() {
        val builder = StringBuilder()
        for (i in 0..params.id) {
            builder.append("\n")
        }
        builder.append(params.id).append(" ").append(sl).append(" - ").append(vl)
        lc?.text = builder.toString()
    }
}