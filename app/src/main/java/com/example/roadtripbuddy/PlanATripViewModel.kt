package com.example.roadtripbuddy

import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PlanATripViewModel : ViewModel() {

    // Holds the list of waypoints AND predicted time spent at said waypoints for Plan A Trip
    private val _planWaypoints = MutableStateFlow(mutableListOf<WaypointItem>())
    val planWaypoints: StateFlow<List<WaypointItem>> = _planWaypoints

    // Holds ETA (Estimated Time of Arrival)
    private val _ETA = MutableStateFlow("")
    val ETA: StateFlow<String> = _ETA


    fun updatePlanWaypoints(index: Int, newAddress: String, newTime: Int){
        _planWaypoints.value = _planWaypoints.value.toMutableList().apply{
            if (index in indices){
                this[index] = WaypointItem(newAddress, newTime)
            }
        }
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


    fun addPlanWaypoint() {
        _planWaypoints.value = _planWaypoints.value.toMutableList().apply {
            add(WaypointItem("", 0))
        }
    }

    fun updateETA(newETA: String) {
        val timeSpentList = planWaypoints.value.map { it.timeSpent }
        var timeTotal: Int = 0
        for (i in timeSpentList){
            timeTotal += i
        }

        _ETA.value = (newETA.toInt() + timeTotal).toString()
    }

    fun clearPlanWaypoints(){
        _planWaypoints.value.clear()
    }

}
