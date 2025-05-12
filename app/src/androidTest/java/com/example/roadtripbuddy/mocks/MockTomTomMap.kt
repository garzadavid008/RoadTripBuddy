package com.example.roadtripbuddy.mocks

import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.map.display.TomTomMap
import com.tomtom.sdk.map.display.camera.CameraOptions
import com.tomtom.sdk.map.display.marker.Marker
import com.tomtom.sdk.map.display.marker.MarkerOptions
import com.tomtom.sdk.map.display.route.Route as DisplayRoute
import com.tomtom.sdk.map.display.route.RouteOptions
import com.tomtom.sdk.routing.route.Route as RoutingRoute
import com.tomtom.sdk.routing.route.Summary
import io.mockk.every
import io.mockk.mockk
import kotlin.time.Duration.Companion.seconds

class MockTomTomMap {
    val mock = mockk<TomTomMap>(relaxed = true)

    fun moveCamera(options: CameraOptions) = mock.moveCamera(options)
    fun addMarker(options: MarkerOptions): Marker = mock.addMarker(options)
    fun addRoute(options: RouteOptions): DisplayRoute = MockDisplayRoute().mock
}

class MockDisplayRoute {
    val mock = mockk<DisplayRoute>(relaxed = true) {
        every { id } returns mockk()
        every { geometry } returns listOf(GeoPoint(0.0, 0.0))
    }
}

class MockRoutingRoute {
    val mock = mockk<RoutingRoute>(relaxed = true) {
        every { id } returns mockk()
        every { geometry } returns listOf(GeoPoint(0.0, 0.0))
        every { summary } returns mockk {
            every { travelTime } returns 600.seconds
        }
    }
}