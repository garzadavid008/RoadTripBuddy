package com.example.roadtripbuddy.SearchDrawer

import android.util.Log
import com.example.roadtripbuddy.SearchDrawerViewModel
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.roadtripbuddy.NavigationMap
import com.example.roadtripbuddy.SuggPlace
import com.tomtom.sdk.search.model.SearchResultType
import com.tomtom.sdk.search.model.result.SearchResult

@Composable
fun LocationDetailsPage(
    viewModel: SearchDrawerViewModel,
    location: SearchResult,
    isRouteReady: MutableState<Boolean>,
    navMap: NavigationMap,
    onBack: () -> Unit,
    onRouteClick: () -> Unit,
    place : SuggPlace?
) {

    val eta by viewModel.ETA.collectAsState()
    var isPoi by remember { mutableStateOf(false) }

    var locationName by remember {
        mutableStateOf(location.place.address?.freeformAddress.orEmpty())
    }

    // 2) whenever the location changes, if it's a POI fire off your lookup
    LaunchedEffect(location) {
        Log.d("locationDetailsPage", "location passed")
        if (location.type == SearchResultType.Poi) {
            Log.d("locationDetailsPage", "location is POI")
            navMap.searchManager.toPoi(location.searchResultId) { poiResult ->
                // safely grab first POI name
                val poiName = poiResult
                    ?.poiDetails
                    ?.poi
                    ?.names
                    ?.firstOrNull()
                    .orEmpty()

                // 3) update your state – Compose will recompose and show the new name
                if (poiName.isNotBlank()) {
                    locationName = poiName
                    isPoi = true
                }
            }
        }
    }
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
            if(isPoi){
                Spacer(modifier = Modifier.height(16.dp))
                Text("Address: ${location?.place?.address?.freeformAddress}", style = MaterialTheme.typography.bodyMedium)
            }


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