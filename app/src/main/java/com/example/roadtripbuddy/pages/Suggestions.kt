package com.example.roadtripbuddy.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Preview
@Composable
fun ()
{

    // each question will have a number, question title, and check boxes with answers
    Box(
        modifier = Modifier
    //       .fillMaxSize()
            .background(Color.White) // 🔹 Change background color here
            .padding(20.dp),
    )
    {

        Row(
            //verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Hello", fontSize = 20.sp)
            Column {

            }
        }
    }
}


@Preview
@Composable
fun Suggestions()
{

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Gray) // 🔹 Change background color here
            .padding(20.dp),
    )
    {
        Column(
            //  verticalAlignment = Alignment.CenterVertically
            // horizontalArrangement = Arrangement.spacedBy(10.dp)
        ){

        }
    }

}