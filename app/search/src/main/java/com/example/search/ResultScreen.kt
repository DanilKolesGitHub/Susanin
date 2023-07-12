package com.example.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.arkivanov.decompose.ComponentContext
import com.example.navigation.NavigationType
import com.example.navigation.ResultScreenParams
import com.example.navigation.context.ScreenContext
import com.example.navigation.screens.Screen
import com.example.navigation.screens.ScreenParams
import com.example.navigation.screens.ViewScreen

class ResultScreen(context: ScreenContext, type: ResultScreenParams): ViewScreen<ResultScreenParams>(context, type) {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
    ): View {
        return inflater.inflate(R.layout.result_layout, container, false)
    }

    override fun onViewCreated(container: View) {
        val text: TextView = container.findViewById(R.id.result)
        text.text = params.result
        text.setOnClickListener {
            navigate(ResultScreenParams(params.result+"0"))
        }
    }

    private fun navigate(resultScreenParams: ResultScreenParams) {
        node.parent!!.findHolder(NavigationType.STACK.name)!!.navigator.open(resultScreenParams)
    }

}