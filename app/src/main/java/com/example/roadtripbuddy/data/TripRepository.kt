package com.example.roadtripbuddy.data

import androidx.datastore.core.DataStore
import com.example.roadtripbuddy.WaypointItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TripsRepository @Inject constructor(
    private val store: DataStore<Trips>
) {
    val tripsFlow: Flow<List<Trip>> = store.data.map { it.tripsList }

    suspend fun addTrip(name: String): Long = withContext(Dispatchers.IO) {
        val id = System.currentTimeMillis()
        val now = System.currentTimeMillis()

        store.updateData { current ->
            current.toBuilder()
                .addTrips(
                    Trip.newBuilder()
                        .setId(id)
                        .setName(name)
                        .setInitialDeparture(now)
                        .build()
                )
                .build()
        }

        id                                           // 3) return it
    }

    suspend fun renameTrip(tripId: Long, newName: String) =
        withContext(Dispatchers.IO) {
            store.updateData { current ->
                val idx = current.tripsList.indexOfFirst { it.id == tripId }
                if (idx == -1) return@updateData current

                val renamed = current.tripsList[idx]
                    .toBuilder()
                    .setName(newName)
                    .build()

                current.toBuilder()
                    .setTrips(idx, renamed)
                    .build()
            }
        }

    suspend fun deleteTrip(tripId: Long) =
        withContext(Dispatchers.IO) {
            store.updateData { current ->
                val remaining = current.tripsList.filterNot { it.id == tripId }
                current.toBuilder()
                    .clearTrips()
                    .addAllTrips(remaining)
                    .build()
            }
        }

    suspend fun saveTrip(
        tripId: Long,
        waypoints: List<WaypointItem>,
        initialDeparture: Long
    ) = withContext(Dispatchers.IO) {

        store.updateData { current ->

            // ── 1. locate the trip ───────────────────────────────────────────────
            val idx = current.tripsList.indexOfFirst { it.id == tripId }
            if (idx == -1) throw IllegalArgumentException("Trip $tripId not found")

            // 2. rebuild the proto with new waypoints
            val builder = current.tripsList[idx]
                .toBuilder()
                .clearWaypoints()
                .addAllWaypoints(waypoints.map { it.toProto() })

            // 3. override initial departure if provided
            initialDeparture.let { builder.setInitialDeparture(it) }

            val updatedTrip = builder.build()

            // ── 3. write it back in the same position ────────────────────────────
            current.toBuilder()
                .setTrips(idx, updatedTrip)
                .build()
        }
    }
}
