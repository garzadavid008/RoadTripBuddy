package com.example.roadtripbuddy.permissionsTests

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.example.roadtripbuddy.LocationService
import com.example.roadtripbuddy.TomTomMapComponent
import com.tomtom.sdk.location.LocationProvider
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
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

    private val mockLocationService = mockk<LocationService>(relaxed = true)
    private val mockLocationProvider = mockk<LocationProvider>(relaxed = true)

    @Before
    fun setup() {
        every { mockLocationService.getLocationProvider() } returns mockLocationProvider
    }

    @Test
    fun locationPermission_promptDisplayed_whenNotGranted() {
        every { mockLocationService.areLocationPermissionsGranted() } returns false

        composeTestRule.setContent {
            TomTomMapComponent(
                apiKey = "test_api_key",
                onMapReady = { /* No-op */ },
                onMapDispose = { /* No-op */ }
            )
        }

        verify { mockLocationService.requestLocationPermissions() }
    }

    @Test
    fun handles_locationOff() {
        every { mockLocationService.areLocationPermissionsGranted() } returns true
        every { mockLocationProvider.isEnabled() } returns false

        composeTestRule.setContent {
            TomTomMapComponent(
                apiKey = "test_api_key",
                onMapReady = { /* No-op */ },
                onMapDispose = { /* No-op */ }
            )
        }

        verify { mockLocationService.getLocationProvider() }
    }

    @Test
    fun map_renders_withPermissionGranted() {
        every { mockLocationService.areLocationPermissionsGranted() } returns true
        every { mockLocationProvider.isEnabled() } returns true

        composeTestRule.setContent {
            TomTomMapComponent(
                apiKey = "test_api_key",
                onMapReady = { /* No-op */ },
                onMapDispose = { /* No-op */ }
            )
        }

        verify { mockLocationService.enableUserLocation() }
    }

    @Test
    fun map_showsError_withPermissionDenied() {
        every { mockLocationService.areLocationPermissionsGranted() } returns false

        composeTestRule.setContent {
            TomTomMapComponent(
                apiKey = "test_api_key",
                onMapReady = { /* No-op */ },
                onMapDispose = { /* No-op */ }
            )
        }

        verify { mockLocationService.requestLocationPermissions() }
    }
}

/*Checklist of Tasks;
* Prompt shows when location is not granted
* behaviour when location is off
* behaviour when permission is granted/denied
* */