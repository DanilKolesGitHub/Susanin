package com.example.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.navigation.FeedTabScreenParams
import com.example.navigation.ResultScreenParams
import com.example.navigation.SearchScreenParams
import com.example.navigation.VideoScreenParams
import com.example.navigation.context.ScreenContext
import com.example.navigation.transaction.transaction
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
    }

}