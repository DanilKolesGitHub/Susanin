package com.example.settings

//
//class SettingsScreen(context: ScreenContext,
//                     screenType: SettingsScreenParams)
//    : Screen<SettingsScreenParams>(context, screenType) {
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup,
//        params: ScreenParams?
//    ): View {
//        childContext()
//        val layout = ComposeView(container.context)
//        layout.setContent {
//            SettingsView(this)
//        }
//        return layout
//    }
//
//    fun expand() {
//
//    }
//
//    fun close() {
//
//    }
//
//}
//
//fun registerSettingsScreens(register: ScreenRegister) {
//    register.registerScreen(SettingsScreenParams::class, object : ScreenFactory<SettingsScreenParams> {
//        override fun create(context: ScreenContext, screenType: SettingsScreenParams): SettingsScreen {
//            return SettingsScreen(context, screenType)
//        }
//    })
//}