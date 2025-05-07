package com.example.roadtripbuddy.permissionsTests

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.example.roadtripbuddy.LocationService
import com.example.roadtripbuddy.MainActivity
import com.example.roadtripbuddy.TomTomMapComponent
import com.tomtom.sdk.map.display.TomTomMap
import io.mockk.every
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

    private lateinit var locationService: LocationService
    private val mainActivity = mockk<MainActivity>(relaxed = true)

    @Before
    fun setup() {
        locationService = LocationService(mainActivity)
    }

    @Test
    fun locationPermission_promptDisplayed_whenNotGranted() {
        every { mainActivity.checkSelfPermission(any()) } returns PackageManager.PERMISSION_DENIED
        composeTestRule.setContent {
            TomTomMapComponent(
                apiKey = "test_api_key",
                onMapReady = { },
                onMapDispose = { }
            )
        }
        composeTestRule.onNodeWithTag("permission_rationale").assertExists()
    }

    @Test
    fun locationOff_showsEnableLocationPrompt() {
        val locationProvider = mockk<com.tomtom.sdk.location.LocationProvider>()
        every { locationProvider.isEnabled() } returns false
        composeTestRule.setContent {
            TomTomMapComponent(
                apiKey = "test_api_key",
                onMapReady = { map ->
                    map.setLocationProvider(locationProvider)
                },
                onMapDispose = { }
            )
        }
        composeTestRule.onNodeWithTag("enable_location_prompt").assertExists()
    }
}

/*This Tests:
* If the location is granted or not.
* Behavior when location is off
* behavior when permission is granted/denied
* Note: Revise
* */
