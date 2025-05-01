package com.example.roadtripbuddy.data

import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.search.model.SearchResultType
import com.tomtom.sdk.search.model.result.SearchResultId

data class WaypointDto(
    val type: SearchResultType,
    val searchResultId: SearchResultId,
    val geoPoint: GeoPoint,
    val name: String?,
)
