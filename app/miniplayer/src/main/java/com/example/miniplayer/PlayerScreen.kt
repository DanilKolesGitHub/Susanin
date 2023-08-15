package com.example.miniplayer

//class PlayerScreen(context: ScreenContext, screenType: PlayerScreenParams): Screen<PlayerScreenParams>(context, screenType) {
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup,
//        params: ScreenParams?
//    ): View {
//        val layout = ComposeView(container.context)
//        layout.setContent {
//            PlayerView(this)
//        }
//        return layout
//    }
//
//    fun expand() {
//
//    }
//
//    fun collapse() {
//
//    }
//
//    fun close() {
//
//    }
//
//}
//
//fun registerPlayerScreens(register: ScreenRegister) {
//    register.registerScreen(PlayerScreenParams::class, object : ScreenFactory<PlayerScreenParams> {
//        override fun create(context: ScreenContext, screenType: PlayerScreenParams): PlayerScreen {
//            return PlayerScreen(context, screenType)
//        }
//    })
//}