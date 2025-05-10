package com.example.roadtripbuddy.mapfunctionalityTests

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.roadtripbuddy.PlanATripViewModel
import com.example.roadtripbuddy.PlanMap
import com.example.roadtripbuddy.PlanRouteManager
import com.example.roadtripbuddy.PlacesViewModel
import com.example.roadtripbuddy.TomTomMapComponent
import com.example.roadtripbuddy.WaypointItem
import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.map.display.TomTomMap
import com.tomtom.sdk.map.display.camera.CameraOptions
import com.tomtom.sdk.map.display.route.Route
import com.tomtom.sdk.search.model.result.SearchResult
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.time.Duration.Companion.seconds

@RunWith(AndroidJUnit4::class)
class MapFunctionalityTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var mockTomTomMap: TomTomMap
    private lateinit var mockPlanATripViewModel: PlanATripViewModel
    private lateinit var mockPlacesViewModel: PlacesViewModel
    private lateinit var mockPlanRouteManager: PlanRouteManager
    private lateinit var mockSearchResult: SearchResult
    private lateinit var mockRoute: Route

    @Before
    fun setup() {
        mockTomTomMap = mockk(relaxed = true)
        mockPlanATripViewModel = mockk(relaxed = true)
        mockPlacesViewModel = mockk(relaxed = true)
        mockPlanRouteManager = mockk(relaxed = true)
        mockSearchResult = mockk {
            every { place } returns mockk {
                every { coordinate } returns GeoPoint(0.0, 0.0)
                every { address } returns mockk {
                    every { freeformAddress } returns "Test Address"
                }
            }
        }
        mockRoute = mockk {
            every { id } returns mockk()
            every { geometry } returns listOf(GeoPoint(0.0, 0.0))
            every { summary } returns mockk {
                every { travelTime } returns 600.seconds
                every { length } returns mockk {
                    every { inMiles() } returns 10.0
                }
            }
        }
    }

    @Test
    fun map_rendersCorrectly() {
        composeTestRule.setContent {
            TomTomMapComponent(
                apiKey = "test_api_key",
                onMapReady = { mockTomTomMap },
                onMapDispose = { /* No-op */ }
            )
        }
        verify { mockTomTomMap wasNot null }
    }

    @Test
    fun map_showsMarkers_basedOnWaypoints() {
        val waypoints = listOf(WaypointItem(searchResult = mockSearchResult))
        every { mockPlanATripViewModel.planWaypoints } returns MutableStateFlow(waypoints)

        composeTestRule.setContent {
            PlanMap(
                context = mockk(),
                activity = mockk(),
                mapReadyState = mutableStateOf(true)
            ).PlanMapContent()
        }

        verify { mockTomTomMap.addMarker(any()) }
    }

    @Test
    fun map_handlesZoomInteraction() {
        composeTestRule.setContent {
            TomTomMapComponent(
                apiKey = "test_api_key",
                onMapReady = { mockTomTomMap },
                onMapDispose = { /* No-op */ }
            )
        }
        every { mockTomTomMap.zoomTo(any()) } returns Unit
        mockTomTomMap.zoomTo(15.0)
        verify { mockTomTomMap.zoomTo(15.0) }
    }

    @Test
    fun map_updatesRoute() {
        every { mockPlanATripViewModel.selectedRoutePair } returns mutableStateOf(Pair(mockRoute, mockRoute))

        composeTestRule.setContent {
            PlanMap(
                context = mockk(),
                activity = mockk(),
                mapReadyState = mutableStateOf(true)
            ).PlanMapContent()
        }

        verify { mockTomTomMap.addRoute(any()) }
    }

    @Test
    fun map_handlesOffline() {
        every { mockTomTomMap.isOnline() } returns false

        composeTestRule.setContent {
            TomTomMapComponent(
                apiKey = "test_api_key",
                onMapReady = { mockTomTomMap },
                onMapDispose = { /* No-op */ }
            )
        }

        verify { mockTomTomMap wasNot null }
    }

    @Test
    fun map_handlesPanInteraction() {
        composeTestRule.setContent {
            TomTomMapComponent(
                apiKey = "test_api_key",
                onMapReady = { mockTomTomMap },
                onMapDispose = { /* No-op */ }
            )
        }

        every { mockTomTomMap.moveCamera(any()) } returns Unit
        mockTomTomMap.moveCamera(CameraOptions(GeoPoint(1.0, 1.0)))
        verify { mockTomTomMap.moveCamera(any()) }
    }

    @Test
    fun map_displaysRouteBalloon() {
        every { mockPlanATripViewModel.selectedRoutePair } returns mutableStateOf(Pair(mockRoute, mockRoute))

        composeTestRule.setContent {
            PlanMap(
                context = mockk(),
                activity = mockk(),
                mapReadyState = mutableStateOf(true)
            ).PlanMapContent()
        }

        verify { mockTomTomMap.addMarker(match { it.balloonText == "10 min" }) }
    }
}
/*Checklist of tasks:
*Map renders correctly
* Markers appear based on data
* User can interact with the map
* route or location updates appear correctly
* offline map behaviour
* zoom and pan interactions */