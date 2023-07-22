package com.example.susanin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.example.navigation.DialogScreenParams
import com.example.navigation.NavigationType
import com.example.navigation.context.ScreenContext
import com.example.navigation.screens.ViewScreen
import com.example.search.R

class DialogScreen(context: ScreenContext, type: DialogScreenParams): ViewScreen<DialogScreenParams>(context, type) {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
    ): View {
        return inflater.inflate(com.example.susanin.R.layout.dialog_layout, container, false)
    }

    override fun onViewCreated(container: View) {
        val input: EditText = container.findViewById(R.id.input)
        val result: Button = container.findViewById(R.id.result)
        input.setText(params.result, TextView.BufferType.EDITABLE)

        result.setOnClickListener {
            navigate(DialogScreenParams(input.text.toString()))
        }
    }

    private fun navigate(resultScreenParams: DialogScreenParams) {
        node.parent!!.findHolder(NavigationType.SLOT.name)!!.navigator.open(resultScreenParams)
    }

}