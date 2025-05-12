package com.example.roadtripbuddy.mocks

import androidx.compose.runtime.MutableState
import com.example.roadtripbuddy.LocationService
import com.example.roadtripbuddy.NavigationMap
import com.tomtom.sdk.location.LocationProvider
import io.mockk.every
import io.mockk.mockk

object MockLocationService {
    val instance: LocationService = mockk(relaxed = true) {
        every { getLocationProvider() } returns mockk()
        every { mapLocationInitializer(any<NavigationMap>(), any<MutableState<Boolean>>()) } returns Unit
        every { enableUserLocation() } returns Unit
        every { requestLocationPermissions() } returns Unit
    }
}