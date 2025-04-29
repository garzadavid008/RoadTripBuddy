package com.example.roadtripbuddy.SearchDrawer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.tomtom.sdk.location.ExperimentalPoiInPlaceApi
import com.tomtom.sdk.search.model.result.SearchResult

@OptIn(ExperimentalPoiInPlaceApi::class)
@Composable
fun LocationDetailsPage(
    location: SearchResult,
    onBack: () -> Unit,
    ) {
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
        Text(location.place.address?.freeformAddress!!, style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))
        //Text("Address: ${location.place.}", style = MaterialTheme.typography.bodyMedium)
        val city = location.place.address?.freeformAddress!!.split(",")?.getOrNull(1)?.trim() ?: "Unknown"
        Text("City: $city ", style = MaterialTheme.typography.bodyMedium)
        Text("Category: ${location.place.poi?.categoryIds?.first()}", style = MaterialTheme.typography.bodyMedium)

        Spacer(modifier = Modifier.height(24.dp))

    }
}