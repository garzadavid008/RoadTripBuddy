package com.example.roadtripbuddy

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.tomtom.sdk.search.common.error.SearchFailure
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

    val selectedLocation = mutableStateOf<SearchResult?>(null)

    fun initializeWaypoints(initialDestination: SearchResult) {
        if (_waypoints.value.isEmpty()) {
            _waypoints.value = mutableListOf(initialDestination)
        }
    }

    fun updateSelectedLocation(location: SearchResult?){
        selectedLocation.value = location
    }

    fun updateWaypoint(index: Int, newValue: SearchResult, onDuplicate: (() -> Unit)? = null) {
        val current = _waypoints.value

        // Check if the new value already exists at a different index
        val isDuplicate = current.withIndex().any { (i, item) ->
            i != index && item?.searchResultId?.id == newValue.searchResultId.id
        }


        if (isDuplicate) {
            onDuplicate?.invoke()
            return
        }

        _waypoints.value = current.toMutableList().apply {
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

    fun addWaypoint(value: SearchResult, onDuplicate: (() -> Unit)? = null) {
        val current = _waypoints.value
        val alreadyExists = current.any { it?.searchResultId?.id == value.searchResultId.id }

        if (!alreadyExists) {
            _waypoints.value = current.toMutableList().apply { add(value) }
        } else {
            onDuplicate?.invoke()
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
