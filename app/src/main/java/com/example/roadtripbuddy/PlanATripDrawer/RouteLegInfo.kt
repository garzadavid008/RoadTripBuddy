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

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

        // ← Back text
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.Filled.ArrowBack,
                contentDescription = "Back"

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

        Card(
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Selected Route", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))

                Text("Distance: $roundedMiles mi")
                Text("Estimated Time: $travelTimeMinutes minutes")
                Text("Depart from: ${routingRoute.routeStops.first().place.address?.freeformAddress}")
                Text("Depart at: $formattedDepartTime")
                Text("Arrive at: ${routingRoute.routeStops.last().place.address?.freeformAddress}")
                Text("Arrive at: $formattedArriveTime")
            }
        }
    }
}

