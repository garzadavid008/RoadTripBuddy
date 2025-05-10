package com.example.roadtripbuddy

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.tomtom.sdk.routing.common.ExperimentalTimeZoneApi
import com.tomtom.sdk.routing.route.Route
import java.text.DecimalFormat
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalTimeZoneApi::class)
@Composable
fun RouteLegInfo(
    selectedRoutePair: Pair<com.tomtom.sdk.map.display.route.Route?, Route?>?,
    onBack: () -> Unit
) {
    val routingRoute = selectedRoutePair?.second

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)
    ) {
        // ← Back button
        IconButton(onClick = { onBack() }) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color(0xFF2ebcff)
            )
        }

        if (routingRoute == null) {
            Text(
                text = "No route selected.",
                style = MaterialTheme.typography.bodyMedium
            )
            return
        }

        val lengthInMiles = routingRoute.summary.length.inMiles()
        val travelTimeMinutes = routingRoute.summary.travelTime.inWholeMinutes
        val formatter = DecimalFormat("#.#")
        val roundedMiles = formatter.format(lengthInMiles)

        val dateFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withZone(ZoneId.systemDefault())
        val formattedDepartTime = dateFormatter.format(routingRoute.summary.departureTime)
        val formattedArriveTime = dateFormatter.format(routingRoute.summary.arrivalTime)

        Spacer(modifier = Modifier.height(8.dp))
        Text("Selected Route", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(12.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            InfoRow(label = "Distance", value = "$roundedMiles mi")
            InfoRow(label = "Estimated Time", value = "$travelTimeMinutes minutes")
            InfoRow(label = "Depart From", value = routingRoute.routeStops.first().place.address?.freeformAddress.orEmpty())
            InfoRow(label = "Depart At", value = formattedDepartTime)
            InfoRow(label = "Arrive At", value = routingRoute.routeStops.last().place.address?.freeformAddress.orEmpty())
            InfoRow(label = "Arrive By", value = formattedArriveTime)
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.width(100.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
