package com.example.roadtripbuddy.mocks

import com.example.roadtripbuddy.LocationService
import com.example.roadtripbuddy.NavigationMap
import com.tomtom.sdk.location.LocationProvider
import io.mockk.mockk

class MockLocationService : LocationService(mockk()) {
    override fun areLocationPermissionsGranted(): Boolean = true
    override fun requestLocationPermissions() {}
    override fun enableUserLocation() {}
    override fun getLocationProvider(): LocationProvider = mockk(relaxed = true)
    override fun mapLocationInitializer(mapInit: NavigationMap, isInitialCameraPositionSetInit: MutableState<Boolean>) {}
}
// mocks its namesake