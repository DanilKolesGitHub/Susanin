package com.example.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.subscribe
import com.example.navigation.InputScreenParams
import com.example.navigation.ResultScreenParams
import com.example.navigation.context.ScreenContext
import com.example.navigation.screens.ViewScreen
import com.example.navigation.transaction.transaction

class InputScreen(context: ScreenContext, type: InputScreenParams): ViewScreen<InputScreenParams>(context, type) {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
    ): View {
        return inflater.inflate(R.layout.input_layout, container, false)
    }

    override fun onViewCreated(container: View) {
        val input: EditText = container.findViewById(R.id.search_input)
        val result: Button = container.findViewById(R.id.result_button)
        val lc: TextView = container.findViewById(R.id.lc_txt)
        val vlc: TextView = container.findViewById(R.id.vlc_txt)
        printlc(lc, lifecycle)
        printlc(vlc, viewLifecycle)

        result.setOnClickListener {
            transaction {
                open(ResultScreenParams(input.text.toString()))
//                open(DialogScreenParams(Color.BLUE))
            }
        }
    }

    private fun navigate(resultScreenParams: ResultScreenParams) {
        transaction { open(resultScreenParams) }
    }

    private fun printlc(textView: TextView, lifecycle: Lifecycle){
        lifecycle.subscribe(
            onCreate = {textView.text = "OnCreate"},
            onStart  = {textView.text = "onStart"},
            onResume = {textView.text = "onResume"},
            onPause = {textView.text = "onPause"},
            onStop = {textView.text = "onStop"},
            onDestroy = {textView.text = "onDestroy"},
        )
    }

}