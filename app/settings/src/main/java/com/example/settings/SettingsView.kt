package com.example.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
//
//@OptIn(ExperimentalMaterialApi::class)
//@Composable
//fun SettingsView(screen: SettingsScreen) {
//    val bottomSheetState = rememberModalBottomSheetState(
//        initialValue = ModalBottomSheetValue.Expanded,
//        skipHalfExpanded = true,
//        confirmStateChange = {
//            when(it){
//                ModalBottomSheetValue.Expanded -> screen.expand()
//                ModalBottomSheetValue.Hidden -> screen.close()
//                else -> { }
//            }
//            true
//        }
//    )
//
//    ModalBottomSheetLayout(
//        sheetState = bottomSheetState,
//        sheetContent = {
//            Box(modifier = Modifier.fillMaxWidth().height(400.dp).background(Color.White)) {
//                Text(text = "Settings", fontSize = 42.sp, color = Color.LightGray, modifier = Modifier.align(Alignment.Center))
//            }
//        }
//    ){
//        Box(Modifier.background(Color.Transparent))
//    }
//}