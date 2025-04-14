package com.example.roadtripbuddy

import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class TripViewModel : ViewModel() {

    // Holds the list of waypoints
    private val _waypoints = MutableStateFlow(mutableListOf<String>())
    val waypoints: StateFlow<List<String>> = _waypoints

    // Holds ETA (Estimated Time of Arrival)
    private val _ETA = MutableStateFlow("")
    val ETA: StateFlow<String> = _ETA

    fun initializeWaypoints(initialDestination: String) {
        if (_waypoints.value.isEmpty()) {
            _waypoints.value = mutableListOf(initialDestination)
        }
    }

    fun updateWaypoint(index: Int, newValue: String) {
        _waypoints.value = _waypoints.value.toMutableList().apply {
            if (index in indices) {
                this[index] = newValue
            }
        }
    }

    fun removeWaypoint(index: Int) {
        // Create a new copy of the list
        val newList = _waypoints.value.toMutableList()

        // Check if index is valid
        if (index in newList.indices) {
            newList.removeAt(index)
            // Update the StateFlow with the new list
            _waypoints.value = newList
            Log.d("Remove waypoint", newList.toString())
        }
    }

    fun addWaypoint() {
        _waypoints.value = _waypoints.value.toMutableList().apply {
            add("")
        }
    }

    fun updateETA(newETA: String) {
        _ETA.value = newETA
    }

    fun clearWaypoints(){
        _waypoints.value.clear()
    }

}
