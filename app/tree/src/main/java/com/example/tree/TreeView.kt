package com.example.tree

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TreeView(uiNode: UiNode) {
    Column(Modifier
        .fillMaxWidth()
        .wrapContentHeight()
        .padding(16.dp)
        .background(color = Color(218,75,31,50))) {
        Text(
            color = Color.DarkGray,
            text = uiNode.name,
            fontSize = 26.sp,
            modifier = Modifier.wrapContentSize().padding(start = 16.dp, top = 4.dp, bottom = 4.dp)
        )
        uiNode.children.forEach {
            TreeView(uiNode = it)
        }
    }
}