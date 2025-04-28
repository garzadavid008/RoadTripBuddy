package com.example.roadtripbuddy

import com.tomtom.sdk.search.model.result.SearchResult

data class WaypointItem(
    var searchResult: SearchResult? = null,
    var hour: Int = 0,
    var minute: Int = 0,
)
