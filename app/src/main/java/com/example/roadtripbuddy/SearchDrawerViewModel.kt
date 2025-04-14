package com.example.roadtripbuddy

import android.util.Log
import androidx.lifecycle.ViewModel
import com.tomtom.sdk.search.model.result.SearchResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SearchDrawerViewModel : ViewModel() {

    // Holds the list of waypoints for the navigation(SearchDrawer/RouteEditPage)
    private val _waypoints = MutableStateFlow(mutableListOf<SearchResult?>())
    val waypoints: StateFlow<List<SearchResult?>> = _waypoints

    // Holds ETA (Estimated Time of Arrival)
    private val _ETA = MutableStateFlow("")
    val ETA: StateFlow<String> = _ETA

    fun initializeWaypoints(initialDestination: SearchResult) {
        if (_waypoints.value.isEmpty()) {
            _waypoints.value = mutableListOf(initialDestination)
        }
    }

    fun updateWaypoint(index: Int, newValue: SearchResult) {
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

    fun addWaypoint(value: SearchResult) {
        _waypoints.value = _waypoints.value.toMutableList().apply {
            add(value)
        }
    }



    fun moveWaypoint(fromIndex: Int, toIndex: Int){
        _waypoints.value = _waypoints.value.toMutableList().also { list ->
            val item = list.removeAt(fromIndex)
            list.add(toIndex, item)
        }
    }

    fun updateETA(newETA: String) {
        _ETA.value = newETA
    }
    
    fun clearWaypoints(){
        _waypoints.value.clear()
    }

}
