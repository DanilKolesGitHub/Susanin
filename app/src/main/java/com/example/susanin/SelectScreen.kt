package com.example.susanin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.example.navigation.DialogsScreenParams
import com.example.navigation.MainScreenParams
import com.example.navigation.SelectScreenParams
import com.example.navigation.SlotScreenParams
import com.example.navigation.StackScreenParams
import com.example.navigation.TreeScreenParams
import com.example.navigation.context.ScreenContext
import com.example.navigation.factory.ScreenFactory
import com.example.navigation.pages.BackBehaviour
import com.example.navigation.pages.CloseBehaviour
import com.example.navigation.pages.PagesHostView
import com.example.navigation.register.ScreenRegister
import com.example.navigation.screens.Screen
import com.example.navigation.screens.ViewScreen
import com.example.navigation.screens.pages
import com.example.navigation.stack.stack
import com.example.navigation.transaction.transaction
import com.example.navigation.view.PagesViewTransition
import com.example.navigation.view.UiParams
import com.example.navigation.view.ViewTransition

class SelectScreen(context: ScreenContext, type: SelectScreenParams): ViewScreen<SelectScreenParams>(context, type) {

    val pages = pages(
        initialPages = listOf(StackScreenParams, SlotScreenParams, DialogsScreenParams, TreeScreenParams),
        initialSelection = 0,
        closeBehaviour = CloseBehaviour.Circle,
        backBehaviour = BackBehaviour.Circle,
    )

    override fun onCreateView(layoutInflater: LayoutInflater, parent: ViewGroup): View {
        return layoutInflater.inflate(R.layout.select_screen, parent, false)
    }

    override fun onViewCreated(view: View) {
        super.onViewCreated(view)
        view.findViewById<PagesHostView>(R.id.pages).observe(
            pages, viewLifecycle,
            object : UiParams {

                override val overlay: Boolean?
                    get() = true
                override val viewTransition: ViewTransition
                    get() = PagesViewTransition
            }
        )
        view.findViewById<Button>(R.id.stack_button).setOnClickListener {
            transaction { open(StackScreenParams) }
        }
        view.findViewById<Button>(R.id.slot_button).setOnClickListener {
            transaction { open(SlotScreenParams) }
        }
        view.findViewById<Button>(R.id.dialogs_button).setOnClickListener {
            transaction { open(DialogsScreenParams) }
        }
        view.findViewById<Button>(R.id.tree_button).setOnClickListener {
            transaction { open(TreeScreenParams) }
        }
    }
}

fun registerSelectScreens(
    register: ScreenRegister,
) {
    register.registerFactory(
        SelectScreenParams::class,
        object : ScreenFactory<SelectScreenParams> {
            override fun create(
                params: SelectScreenParams,
                context: ScreenContext
            ): Screen<SelectScreenParams> {
                return SelectScreen(context, params)
            }
        }
    )
    register.registerNavigation(MainScreenParams::class) {
        stack(SelectScreenParams::class)
    }
}