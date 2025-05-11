package com.example.roadtripbuddy.data

import androidx.datastore.core.DataStore
import com.example.roadtripbuddy.AuthState
import com.example.roadtripbuddy.IAuthViewModel
import com.example.roadtripbuddy.WaypointItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.security.PrivateKey
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TripsRepository @Inject constructor(
    private val store: DataStore<Trips>,
    private val auth : FirebaseAuth,
    private val fireStore: FirebaseFirestore,
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

       val updatedState: Trips =  store.updateData { current ->

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
        // saving to firestore
        // grabbing the users id
//        val uid = auth.currentUser?.uid
//
//        // 1) Create the list of waypoint‐maps exactly as before:
//        val waypointMaps: List<Map<String,Any>> = waypoints
//            .map { it.toDto() }
//            .map { dto -> dto.toFirestoreMap() }
//
//// 2) Grab the proto instance from your updatedState:
//        val updatedProto = updatedState.tripsList
//            .first { it.id == tripId }  // this is your Trip proto
//
//// 3) Build the Firestore payload, explicitly a Map<String,Any>:
//        val tripMap: Map<String,Any> = mapOf(
//            "id"                to updatedProto.id,
//            "name"              to updatedProto.name,
//            "initial_departure" to updatedProto.initialDeparture,
//            "waypoints"         to waypointMaps
//        )
//
//        // condition only runs if the user is logged in
//        if (uid != null ) {
//            fireStore
//                .collection("users")
//                .document(uid)
//                .collection("roadtripbuddy_trips")
//                .document(tripId.toString())
//                .set(tripMap)
//                .await()
//        }
    }

//    // load trip function
//    suspend fun loadTripFromFirestore(tripId: Long): Trip? = withContext(Dispatchers.IO) {
//        val uid = auth.currentUser?.uid ?: return@withContext null
//
//        val snap = fireStore
//            .collection("users")
//            .document(uid)
//            .collection("roadtripbuddy_trips")
//            .document(tripId.toString())
//            .get()
//            .await()
//
//        val data = snap.data ?: return@withContext null
//
//        // 1) grab the same list of maps you just wrote
//        val raw = (data["waypoints"] as? List<Map<String,Any>>) ?: emptyList()
//
//        // 2) rebuild each Waypoint proto via your existing gson + toDomain()
//        val protos = raw.map { m ->
//            Waypoint.newBuilder()
//                .setSearchJson(m["searchJson"] as String)
//                .setHour((m["hour"]   as Number).toInt())
//                .setMinute((m["minute"] as Number).toInt())
//                .build()
//        }
//
//        // 3) assemble a Trip proto that your UI already knows how to render
//        return@withContext Trip.newBuilder()
//            .setId(tripId)
//            .setName(data["name"] as String)
//            .setInitialDeparture((data["initial_departure"] as Number).toLong())
//            .addAllWaypoints(protos)
//            .build()
//    }
suspend fun loadTripFromFirestore(tripId: Long): Trip? = withContext(Dispatchers.IO) {
    // 1) DataStore lookup
    val dsSnapshot = store.data.first()
    val local = dsSnapshot.tripsList
        .firstOrNull { it.id == tripId }
    if (local != null) return@withContext local

    // 2) Firestore fallback
    val uid = auth.currentUser?.uid ?: return@withContext null
    val snap = fireStore
        .collection("users")
        .document(uid)
        .collection("roadtripbuddy_trips")
        .document(tripId.toString())
        .get()
        .await()
    val data = snap.data ?: return@withContext null

    // 3) Rebuild waypoint protos
    val raw = (data["waypoints"] as? List<Map<String,Any>>) ?: emptyList()
    val protos = raw.map { m ->
        Waypoint.newBuilder()
            .setSearchJson(m["searchJson"] as String)
            .setHour((m["hour"]   as Number).toInt())
            .setMinute((m["minute"] as Number).toInt())
            .build()
    }

    // 4) Assemble the Trip proto
    val fetched = Trip.newBuilder()
        .setId(tripId)
        .setName(data["name"] as String)
        .setInitialDeparture((data["initial_departure"] as Number).toLong())
        .addAllWaypoints(protos)
        .build()

    // 5) Cache into DataStore
    store.updateData { current ->
        val updatedList = current.tripsList
            .filterNot { it.id == tripId }
            .plus(fetched)
        current.toBuilder()
            .clearTrips()
            .addAllTrips(updatedList)
            .build()
    }

    return@withContext fetched
}


}
