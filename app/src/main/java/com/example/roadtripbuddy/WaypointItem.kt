package com.example.roadtripbuddy

import com.tomtom.sdk.search.model.result.SearchResult
import java.util.Date

data class WaypointItem(
    var searchResult: SearchResult? = null,
    var hour: Int = 0,
    var minute: Int = 0,
    var departAt: Date? = null
)
