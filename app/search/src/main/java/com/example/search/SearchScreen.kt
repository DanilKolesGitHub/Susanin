package com.example.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.navigation.InputScreenParams
import com.example.navigation.ResultScreenParams
import com.example.navigation.SearchScreenParams
import com.example.navigation.context.ScreenContext
import com.example.navigation.factory.ScreenFactory
import com.example.navigation.register.ScreenRegister
import com.example.navigation.screens.ViewScreen
import com.example.navigation.screens.stack
import com.example.navigation.stack.StackHostView
import com.example.navigation.stack.stack
import com.example.navigation.view.ForwardBackwardTransition

class SearchScreen(context: ScreenContext, type: SearchScreenParams): ViewScreen<SearchScreenParams>(context, type) {

    val stack = stack(InputScreenParams, true)

    override fun onCreateView(layoutInflater: LayoutInflater, parent: ViewGroup): View {
        return layoutInflater.inflate(R.layout.search_layout, parent, false)
    }

    override fun onViewCreated(view: View) {
        val routerView: StackHostView = view.findViewById(R.id.search_host)
        routerView.observe(stack, viewLifecycle, ForwardBackwardTransition)
    }
}

fun registerSearchScreens(
    register: ScreenRegister,
) {

    register.registerFactory(SearchScreenParams::class, object : ScreenFactory<SearchScreenParams> {
        override fun create(screenType: SearchScreenParams, context: ScreenContext): SearchScreen {
            return SearchScreen(context, screenType)
        }
    })
    register.registerFactory(InputScreenParams::class, object : ScreenFactory<InputScreenParams> {
        override fun create(screenType: InputScreenParams, context: ScreenContext): InputScreen {
            return InputScreen(context, screenType)
        }
    })
    register.registerFactory(ResultScreenParams::class, object : ScreenFactory<ResultScreenParams> {
        override fun create(screenType: ResultScreenParams, context: ScreenContext): ResultScreen {
            return ResultScreen(context, screenType)
        }
    })

    register.registerNavigation(SearchScreenParams){
        stack(
            InputScreenParams::class,
            ResultScreenParams::class)
    }
}