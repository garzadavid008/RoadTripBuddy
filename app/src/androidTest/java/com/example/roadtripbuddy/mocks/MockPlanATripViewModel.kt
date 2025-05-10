package com.example.roadtripbuddy.mocks

import com.example.roadtripbuddy.PlanATripViewModel
import com.example.roadtripbuddy.WaypointItem
import com.tomtom.sdk.map.display.route.Route
import com.tomtom.sdk.search.model.result.SearchResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Date

class MockPlanATripViewModel : PlanATripViewModel() {
    override val planWaypoints: StateFlow<List<WaypointItem>> = MutableStateFlow(emptyList())
    override val ETA: StateFlow<String> = MutableStateFlow("")
    override val initialDeparture: StateFlow<Date> = MutableStateFlow(Date())
    override val selectedRoutePair: MutableState<Pair<Route?, com.tomtom.sdk.routing.route.Route?>?> = mutableStateOf(null)
    override val selectedWaypoint: MutableState<WaypointItem?> = mutableStateOf(null)

    override fun addPlanWaypoint(searchResult: SearchResult) {}
    override fun updateETA(newETA: Duration) {}
    override fun updateSelectedWaypoint(waypoint: WaypointItem?) {}
    override fun setSelectedRoutePair(routePair: Pair<Route, com.tomtom.sdk.routing.route.Route>?) {}
}