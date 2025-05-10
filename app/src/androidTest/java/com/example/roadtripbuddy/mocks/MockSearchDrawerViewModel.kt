package com.example.roadtripbuddy.mocks

import com.example.roadtripbuddy.SearchDrawerViewModel
import com.tomtom.sdk.search.model.result.SearchResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MockSearchDrawerViewModel : SearchDrawerViewModel() {
    override val waypoints: StateFlow<List<SearchResult?>> = MutableStateFlow(emptyList())
    override val ETA: StateFlow<String> = MutableStateFlow("")
    override fun initializeWaypoints(initialDestination: SearchResult) {}
    override fun updateWaypoint(index: Int, newValue: SearchResult) {}
    override fun removeWaypoint(index: Int) {}
    override fun addWaypoint(value: SearchResult) {}
    override fun moveWaypoint(fromIndex: Int, toIndex: Int) {}
    override fun updateETA(newETA: String) {}
    override fun clearWaypoints() {}
}