package com.example.susanin

import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import com.example.navigation.DialogScreenParams
import com.example.navigation.context.ScreenContext
import com.example.navigation.screens.ViewScreen
import com.example.navigation.transaction.transaction

class DialogScreen(context: ScreenContext, type: DialogScreenParams): ViewScreen<DialogScreenParams>(context, type) {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup,
    ): View {
        return inflater.inflate(R.layout.dialog_layout, container, false).apply {
            layoutParams = FrameLayout.LayoutParams(layoutParams).apply {
                height = when(params.color){
                    Color.BLUE -> 1600
                    Color.RED -> 1300
                    Color.GREEN -> 1000
                    else -> 500
                }
                gravity = Gravity.BOTTOM
            }
        }
    }

    override fun onViewCreated(container: View) {
        container.setBackgroundColor(params.color)
        container.findViewById<Button>(R.id.dialog_blue_button).setOnClickListener{
            navigate(Color.BLUE)
        }
        container.findViewById<Button>(R.id.dialog_red_button).setOnClickListener{
            navigate(Color.RED)
        }
        container.findViewById<Button>(R.id.dialog_green_button).setOnClickListener{
            navigate(Color.GREEN)
        }
    }

    private fun navigate(color: Int) {
       transaction { open(DialogScreenParams(color)) }
    }

}