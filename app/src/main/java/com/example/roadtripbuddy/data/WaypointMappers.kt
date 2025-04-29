package com.example.roadtripbuddy.data

import com.example.roadtripbuddy.WaypointItem
import com.google.gson.Gson
import com.tomtom.sdk.location.Address
import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.location.Place
import com.tomtom.sdk.search.model.result.SearchResult
import java.util.Date

private val gson by lazy { Gson() }

fun WaypointItem.toDto() = WaypointDto(
    type = searchResult!!.type,
    searchResultId = searchResult!!.searchResultId,
    geoPoint = searchResult!!.place.coordinate,
    name = searchResult!!.place.address?.freeformAddress
)

fun WaypointDto.toSearchResult(): SearchResult {
    val place = Place(
        coordinate = geoPoint,
        address = Address(freeformAddress = name!!)
    )

    return SearchResult(
        type            = type,
        searchResultId  = searchResultId,
        place           = place
    )
}

fun WaypointItem.toProto(): Waypoint =
    Waypoint.newBuilder()
        .setSearchJson(gson.toJson(toDto()))          // full SearchResult as JSON
        .setHour(hour)
        .setMinute(minute)
        .build()

fun Waypoint.toDomain(): WaypointItem {
    val dto = gson.fromJson(searchJson, WaypointDto::class.java)

    return WaypointItem(
        searchResult = dto.toSearchResult(),
        hour         = hour,
        minute       = minute
    )
}