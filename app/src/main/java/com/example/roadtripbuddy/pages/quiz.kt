package com.example.roadtripbuddy.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


//
@Preview
@Composable
fun Question(number:String ="",question:String ="", ans: List<String> = emptyList())
{
    var checked by remember { mutableStateOf(false) }
    val list: List<Pair<String, Boolean>>
    // each question will have a number, question title, and check boxes with answers
    Row(
        //verticalAlignment = Alignment.CenterVertically
    ){
        Text(text = "$number)", fontSize = 20.sp)
        Column {
            // question
            Text(text = question, fontSize = 20.sp)
            Row( modifier = Modifier.padding(end = 8.dp)) {
                for(item in ans)
                {
                    Text(
                        text = item,
                        fontSize = 8.sp
                    )
                    Checkbox(
                        checked = checked,
                        onCheckedChange = { checked = it }
                    )



                }

            }

        }
    }
}

// page for suggestion quiz
@Preview
@Composable
fun Quiz() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.LightGray) // 🔹 Change background color here
            .padding(20.dp),
        )

    {
        Column(
          //  verticalAlignment = Alignment.CenterVertically
           // horizontalArrangement = Arrangement.spacedBy(10.dp)
        ){
            Question("1","What spots are you interested in?", listOf("Restaurants", "Gas Stations","Attractions","Hotels"))
            // Adding a Spacer of height 20dp
            Spacer(modifier = Modifier.height(20.dp))
            Question("2","Filter Star Ratings", listOf("< 1", "< 2","< 3","< 4","< 5"))
            Spacer(modifier = Modifier.height(20.dp))
            Question("3","Distance", listOf("~5 miles ", "~15 miles","~25 miles"))
            Spacer(modifier = Modifier.height(20.dp))
            Question("4","Filter Traffic status", listOf("Little", "Moderate","High"))
            Spacer(modifier = Modifier.height(20.dp))
            Question("5","Popularity", listOf("Little", "Moderate","High"))
            Spacer(modifier = Modifier.height(20.dp))

        }
    }

}