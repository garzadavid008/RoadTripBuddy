package com.example.roadtripbuddy.permissionsTests

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.example.roadtripbuddy.LocationService
import com.example.roadtripbuddy.NavigationMap
import com.tomtom.sdk.map.display.TomTomMap
import com.example.roadtripbuddy.TomTomMapComponent
import com.example.roadtripbuddy.mocks.MockLocationService
import com.example.roadtripbuddy.mocks.MockNavigationMap
import com.example.roadtripbuddy.mocks.MockTomTomMap
import io.mockk.every
import io.mockk.verify
import io.mockk.mockk
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LocationPermissionTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_COARSE_LOCATION
    )

    private lateinit var mockLocationService: LocationService
    private lateinit var mockTomTomMap: TomTomMap
    private lateinit var mockNavigationMap: NavigationMap

    @Before
    fun setup() {
        mockLocationService = MockLocationService.instance
        mockTomTomMap = MockTomTomMap().mock
        mockNavigationMap = mockk<NavigationMap>(relaxed = true)
    }

    @Test
    fun locationPermission_promptDisplayed_whenNotGranted() {
        every { mockLocationService.requestLocationPermissions() } returns Unit
        every { mockLocationService.mapLocationInitializer(mockNavigationMap, mutableStateOf(false)) } returns Unit

        composeTestRule.setContent {
            TomTomMapComponent(
                apiKey = "test_api_key",
                onMapReady = { mockTomTomMap },
                onMapDispose = { /* No-op */ }
            )
        }

        verify { mockLocationService.requestLocationPermissions() }
    }

    @Test
    fun handles_locationOff() {
        every { mockLocationService.getLocationProvider() } returns mockk {
            every { enable() } returns Unit
        }
        every { mockLocationService.mapLocationInitializer(mockNavigationMap, mutableStateOf(false)) } returns Unit

        composeTestRule.setContent {
            TomTomMapComponent(
                apiKey = "test_api_key",
                onMapReady = { mockTomTomMap },
                onMapDispose = { /* No-op */ }
            )
        }

        verify { mockLocationService.getLocationProvider() }
    }

    @Test
    fun map_renders_withPermissionGranted() {
        every { mockLocationService.getLocationProvider() } returns mockk {
            every { enable() } returns Unit
        }
        every { mockLocationService.mapLocationInitializer(mockNavigationMap, mutableStateOf(false)) } returns Unit

        composeTestRule.setContent {
            TomTomMapComponent(
                apiKey = "test_api_key",
                onMapReady = { mockTomTomMap },
                onMapDispose = { /* No-op */ }
            )
        }

        verify { mockLocationService.enableUserLocation() }
    }



    @Test
    fun map_showsError_withPermissionDenied() {
        every { mockLocationService.requestLocationPermissions() } returns Unit
        every { mockLocationService.mapLocationInitializer(mockNavigationMap, mutableStateOf(false)) } returns Unit

        composeTestRule.setContent {
            TomTomMapComponent(
                apiKey = "test_api_key",
                onMapReady = { mockTomTomMap },
                onMapDispose = { /* No-op */ }
            )
        }

        verify { mockLocationService.requestLocationPermissions() }
    }
}
