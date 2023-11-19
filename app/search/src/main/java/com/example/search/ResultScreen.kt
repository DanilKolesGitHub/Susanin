package com.example.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.transition.Transition
import com.example.navigation.ResultScreenParams
import com.example.navigation.context.ScreenContext
import com.example.navigation.screens.ViewScreen
import com.example.navigation.view.UiParams

class ResultScreen(context: ScreenContext, type: ResultScreenParams): ViewScreen<ResultScreenParams>(context, type), UiParams {

    override val overlay = true
    override val transition: Transition? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
    ): View {
        return inflater.inflate(R.layout.result_layout, container, false)
    }

    override fun onViewCreated(container: View) {
        val text: TextView = container.findViewById(R.id.result)
        text.text = params.result
    }

}