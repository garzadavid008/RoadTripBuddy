package com.example.roadtripbuddy.SearchDrawer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun WaypointTextField(
    address: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .width(350.dp)
            .height(56.dp)
            .clip(RoundedCornerShape(50))  // Pill shape
            .background(Color.White)
            .border(width = 1.dp, color = Color.Gray, shape = RoundedCornerShape(50))
            .padding(horizontal = 11.dp, vertical = 14.dp)
            .clickable { onClick() },
        contentAlignment = Alignment.CenterStart
    ) {
        // Show the address or a placeholder if address is empty.
        Text(
            text = address,
            style = TextStyle(
                fontSize = 14.sp,
                color = Color.Black
            )
        )
    }
}

