package com.example.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import com.example.navigation.InputScreenParams
import com.example.navigation.NavigationType
import com.example.navigation.ResultScreenParams
import com.example.navigation.context.ScreenContext
import com.example.navigation.screens.ViewScreen

class InputScreen(context: ScreenContext, type: InputScreenParams): ViewScreen<InputScreenParams>(context, type) {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
    ): View {
        return inflater.inflate(R.layout.input_layout, container, false)
    }

    override fun onViewCreated(container: View) {
        val input: EditText = container.findViewById(R.id.input)
        val result: Button = container.findViewById(R.id.result)

        result.setOnClickListener {
            navigate(ResultScreenParams(input.text.toString()))
        }
    }

    private fun navigate(resultScreenParams: ResultScreenParams) {
        node.parent!!.findHolder(NavigationType.STACK.name)!!.navigator.open(resultScreenParams)
    }

}