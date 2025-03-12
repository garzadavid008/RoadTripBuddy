package com.example.roadtripbuddy.pages

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.roadtripbuddy.R

// data class to hold the locations data
data class Places (var name:String, var descrip:String)




@Preview
@Composable
fun  placeCard(name:String="",description:String ="")
{
    // card for suggested place
    OutlinedCard(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        border = BorderStroke(1.dp, Color.Black),
        modifier = Modifier
            .size(width = 270.dp, height = 100.dp).padding(10.dp)
             ,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(painter = painterResource(id = R.drawable.filler), contentDescription = "Filler image")
            Column {
                Text("Place $name", modifier = Modifier.padding(10.dp))
                Text("Place $description",modifier = Modifier.padding(10.dp))

            }
        }
    }
}


// since we're not sure how long the number of suggested places is going to be, we can use LazyColumns
@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun Suggestions(placesList:List<Places> = emptyList())
{
    Scaffold(
        topBar = {
            TopAppBar(
                colors = topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Text("Suggested Locations")
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(innerPadding)  // Apply Scaffold's inner padding
                .padding(16.dp),        // Add any additional padding you need
            contentPadding = PaddingValues(16.dp)
        ) {
            // list of suggestions here
            item {
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .wrapContentHeight()
                        .padding(vertical = 25.dp),
                    horizontalArrangement = Arrangement.Center, // center the data
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Discover More",
                       // style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            // this will generate the list of places
            items(placesList)
            { place ->
                placeCard(place.name,place.descrip)
            }
        }

    }
}