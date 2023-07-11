package com.example.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.childStack
import com.example.navigation.*
import com.example.navigation.context.ScreenContext
import com.example.navigation.router.ScreenRegister
import com.example.navigation.screens.ScreenFactory
import com.example.navigation.screens.ScreenParams

//class SearchScreen(context: ScreenContext, type: SearchScreenParams): HostScreen<SearchScreenParams>(context, type) {
//
//    val stack = Router.stack(SearchScreenParams::class)
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup,
//        params: ScreenParams?
//    ): View {
//        childStack()
//        return inflater.inflate(R.layout.search_layout, container, false).also { init(it) }
//    }
//
//    private fun init(container: View) {
//        val routerView: StackView = container.findViewById(R.id.host)
//        routerView.observeStack(stack, lifecycle)
//    }
//}

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