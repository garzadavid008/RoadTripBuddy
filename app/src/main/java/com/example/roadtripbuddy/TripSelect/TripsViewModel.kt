package com.example.roadtripbuddy.TripSelect

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.roadtripbuddy.WaypointItem
import com.example.roadtripbuddy.data.Trip
import com.example.roadtripbuddy.data.TripsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class TripsViewModel @Inject constructor(
    private val repo: TripsRepository
) : ViewModel() {

    val trips = repo.tripsFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun getTrip(id: Long): Trip? =
        trips.value.firstOrNull { it.id == id }

    suspend fun addTrip(name: String): Long = repo.addTrip(name)

    fun deleteTrip(tripId: Long) =
        viewModelScope.launch { repo.deleteTrip(tripId) }

    fun renameTrip(tripId: Long, name: String) =
        viewModelScope.launch { repo.renameTrip(tripId, name) }

    fun saveTrip(tripId: Long, waypoints: List<WaypointItem>, initialDeparture: Long) =
        viewModelScope.launch { repo.saveTrip(tripId, waypoints, initialDeparture) }
}
