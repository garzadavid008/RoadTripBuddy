package com.example.roadtripbuddy.SearchDrawer

import PlacesViewModel
import SuggPlace
import com.example.roadtripbuddy.TripViewModel
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.roadtripbuddy.pages.placeCard

@Composable
fun LocationDetailsPage(
    viewModel: TripViewModel,
    locationName: String,
    isRouteReady: MutableState<Boolean>,
    onBack: () -> Unit,
    onRouteClick: () -> Unit,
    place : SuggPlace?
) {

    val eta by viewModel.ETA.collectAsState()
    LaunchedEffect(eta) {
        isRouteReady.value = eta.isNotEmpty()
    }

    if (isRouteReady.value) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            // Back button
            Box(
                modifier = Modifier
                    .clickable { onBack() }
                    .padding(bottom = 16.dp)
            ) {
                Text("← Back", color = Color.Blue)
            }

            // Location details
            Text(locationName, style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Address: ${place?.address}", style = MaterialTheme.typography.bodyMedium)
            val city = place?.address?.split(",")?.getOrNull(1)?.trim() ?: "Unknown"
            Text("City: $city ", style = MaterialTheme.typography.bodyMedium)
            Text("Category: ${place?.category}", style = MaterialTheme.typography.bodyMedium)

            Spacer(modifier = Modifier.height(24.dp))

            // Route button
            Button(
                onClick = {
                    onRouteClick()
                    isRouteReady.value = false
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6ACFFF),
                    contentColor = Color.White
                )
            ){
                Text(eta)
            }
        }
    }
}