package com.example.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.navigation.*
import com.example.navigation.context.ScreenContext
import com.example.navigation.router.ScreenRegister
import com.example.navigation.screens.ScreenFactory
import com.example.navigation.screens.ViewScreen
import com.example.navigation.stack.StackHostView
import com.example.navigation.stack.stack

class SearchScreen(context: ScreenContext, type: SearchScreenParams): ViewScreen<SearchScreenParams>(context, type) {

    val stack = stack(InputScreenParams)

    override fun onCreateView(layoutInflater: LayoutInflater, parent: ViewGroup): View {
        return layoutInflater.inflate(R.layout.search_layout, parent, false)
    }

    override fun onViewCreated(view: View) {
        val routerView: StackHostView = view.findViewById(R.id.host)
        routerView.observe(stack, viewLifecycle)
    }
}

fun registerSearchScreens(register: ScreenRegister) {
//    register.registerHostScreen(
//        SearchScreenParams(),
//        object : NavigatorFactory{
//            override fun create(): Behaviour {
//                return StackBehaviour()
//            }
//        },
//        InputScreenParams::class,
//        ResultScreenParams::class,
//    )
//    register.registerScreen(
//        SearchScreenParams::class,
//        object : ScreenFactory<SearchScreenParams> {
//            override fun create(context: ComponentContext, screenType: SearchScreenParams): SearchScreen {
//                return SearchScreen(context, screenType)
//            }
//        }
//    )

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
}