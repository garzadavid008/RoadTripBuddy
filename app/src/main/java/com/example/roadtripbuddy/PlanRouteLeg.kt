package com.example.roadtripbuddy

data class RouteLeg(
    var from: WaypointItem,
    var to: WaypointItem,
    var eta: String
)