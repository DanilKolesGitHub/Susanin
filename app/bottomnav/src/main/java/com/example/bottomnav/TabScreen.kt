package com.example.bottomnav

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.bringToFront
import com.arkivanov.decompose.value.observe
import com.example.navigation.*
//import com.example.navigation.router.Router.stack
//import com.example.navigation.router.Router.navigator
//import com.example.navigation.router.Behaviour
//import com.example.navigation.router.TabBehaviour
//import com.example.navigation.screens.ScreenFactory
//import com.example.navigation.screens.ScreenParams
//import com.google.android.material.bottomnavigation.BottomNavigationView
//
//class TabScreen(context: ComponentContext, type: TabScreenParams): HostScreen<TabScreenParams>(context, type) {
//
//    val navigator = navigator()
//
//    val bottomMap = hashMapOf(
//        R.id.feed to FeedScreenParams(),
//        R.id.video to VideoScreenParams(),
//        R.id.tree to TreeScreenParams(),
//    )
//
//    val stack = stack(bottomMap[R.id.feed]!!)
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup,
//        params: ScreenParams?
//    ): View {
//        return inflater.inflate(R.layout.bottom_nav_layout, container, false).also { init(it) }
//    }
//
//    private fun init(container: View) {
//        val routerView: StackView = container.findViewById(R.id.host)
//        val bottom = container.findViewById<BottomNavigationView>(R.id.bottom)
//        routerView.observeStack(stack, lifecycle)
//        stack.observe(lifecycle) {
//            val newId = findMenuId(it.active.configuration)
//            if (newId != bottom.selectedItemId)
//                bottom.selectedItemId = newId
//        }
//        bottom.setOnItemSelectedListener {
//            val screen = bottomMap[it.itemId]!!
//            navigator.bringToFront(screen)
//            return@setOnItemSelectedListener true
//        }
//    }
//
//    private fun findMenuId(active: ScreenParams): Int {
//        bottomMap.forEach {
//            if (active::class == it.value::class)
//                return it.key
//        }
//        throw IllegalStateException()
//    }
//}
//
//fun registerBottomScreens(register: ScreenRegister) {
//    register.registerHostScreen(
//        TabScreenParams(),
//        object : NavigatorFactory {
//            override fun create(): Behaviour {
//                return TabBehaviour()
//            }
//        },
//        FeedScreenParams::class,
//        VideoScreenParams::class,
//        TreeScreenParams::class,
//    )
//
//    register.registerScreen(
//        TabScreenParams::class,
//        object : ScreenFactory<TabScreenParams> {
//            override fun create(context: ComponentContext, screenType: TabScreenParams): TabScreen {
//                return TabScreen(context, screenType)
//            }
//        }
//    )
//}