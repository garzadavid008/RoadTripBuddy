package com.example.roadtripbuddy

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.roadtripbuddy.data.Trip
import com.example.roadtripbuddy.data.toDomain
import com.tomtom.sdk.map.display.marker.Marker
import com.tomtom.sdk.map.display.route.Route
import com.tomtom.sdk.search.model.result.SearchResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Date
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

class PlanATripViewModel : ViewModel() {

    // Holds the list of waypoints AND predicted time spent at said waypoints for Plan A Trip
    private val _planWaypoints = MutableStateFlow(mutableListOf<WaypointItem>())
    val planWaypoints: StateFlow<List<WaypointItem>> = _planWaypoints

    // Holds ETA (Estimated Time of Arrival)
    private val _ETA = MutableStateFlow("")
    val ETA: StateFlow<String> = _ETA

    private val _initialDeparture = MutableStateFlow(Date())
    val initialDeparture: StateFlow<Date> = _initialDeparture

    //To keep track of what route leg is being clicked on at the moment
    val selectedRoutePair = mutableStateOf<Pair<Route?, com.tomtom.sdk.routing.route.Route?>?>(null)

    //To keep track of what waypoint is clicked on at the moment
    val selectedWaypoint = mutableStateOf<WaypointItem?>(null)

    fun loadTrip(trip: Trip) {
        updateInitialDeparture(Date(trip.initialDeparture))

        val items = trip.waypointsList
            .map { it.toDomain() }
            .toMutableList()

        _planWaypoints.value = items
    }

    fun updateSearchResult(index: Int, newSearchResult: SearchResult) {
        _planWaypoints.value = _planWaypoints.value.toMutableList().apply {
            if (index in indices) {
                this[index] = this[index].copy(
                    searchResult = newSearchResult
                )
            }
        }
    }

    fun updateTimeSpent(index: Int, newHour: Int, newMinute: Int){
        _planWaypoints.value = _planWaypoints.value.toMutableList().apply {
            if (index in indices) {
                this[index] = this[index].copy(
                    hour = newHour,
                    minute = newMinute
                )
            }
        }
    }

    fun updateSelectedWaypoint(waypoint:WaypointItem?){
        selectedWaypoint.value = waypoint
    }

    fun setSelectedRoutePair(routePairs: Pair<Route?, com.tomtom.sdk.routing.route.Route?>?){
        selectedRoutePair.value = routePairs
    }

    fun removePlanWaypoint(index: Int){
        val newList = _planWaypoints.value.toMutableList()

        // Check if index is valid
        if (index in newList.indices) {
            newList.removeAt(index)
            // Update the StateFlow with the new list
            _planWaypoints.value = newList
            Log.d("Remove plan waypoint", newList.toString())
        }
    }

    fun moveWaypoint(fromIndex: Int, toIndex: Int){
        _planWaypoints.value = _planWaypoints.value.toMutableList().also { list ->
            val item = list.removeAt(fromIndex)
            list.add(toIndex, item)
        }
    }

    fun addPlanWaypoint(searchResult: SearchResult) {
        _planWaypoints.value = _planWaypoints.value.toMutableList().apply {
            add(WaypointItem(searchResult = searchResult, hour= 0, minute = 0))
        }
    }

    fun updateETA(newETA: Duration) {
        // We get the total minutes of time spent at waypoints by adding all the hour and minutes and turning it into minutes
        val sumOfIntMinutes = planWaypoints.value.sumOf { it.hour * 60 + it.minute }
        // Convert the integer of minutes into a Duration data class
        val totalDuration = sumOfIntMinutes.minutes

        // We then add the newETA with the totalDuration of time spent at waypoints to get the proper
        // ETA in string form
        _ETA.value = newETA.plus(totalDuration).toString()
    }

    fun updateInitialDeparture(newDate: Date){
        _initialDeparture.value = newDate
    }

    fun clearPlanWaypoints(){
        _planWaypoints.value.clear()
    }

}
