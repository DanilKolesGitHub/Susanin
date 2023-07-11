package com.example.miniplayer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
//
//@OptIn(ExperimentalMaterialApi::class)
//@Composable
//fun PlayerView(screen: PlayerScreen) {
//    val scaffoldState: BottomSheetScaffoldState = rememberBottomSheetScaffoldState(
//        bottomSheetState = rememberBottomSheetState(
//            BottomSheetValue.Expanded,
//            confirmStateChange = {
//                when(it){
//                    BottomSheetValue.Expanded -> screen.expand()
//                    BottomSheetValue.Collapsed -> screen.collapse()
//                }
//                true
//            }
//        ),
//    )
//    BottomSheetScaffold(
//        scaffoldState = scaffoldState,
//        sheetContent = {
//            Box(modifier = Modifier.fillMaxSize().background(Color.LightGray)){
//                IconButton(
//                    modifier = Modifier.align(Alignment.TopEnd),
//                    onClick = { screen.close() }) {
//                    Icon(
//                        Icons.Rounded.Close,
//                        contentDescription = "Play"
//                    )
//                }
//            }
//        }) {
//        Box(Modifier.background(Color.Transparent))
//    }
//}