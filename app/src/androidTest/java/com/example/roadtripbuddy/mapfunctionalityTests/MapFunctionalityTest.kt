package com.example.roadtripbuddy.mapfunctionalityTests

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.roadtripbuddy.PlanMap
import com.example.roadtripbuddy.WaypointItem
import com.example.roadtripbuddy.mocks.MockPlanATripViewModel
import com.example.roadtripbuddy.mocks.MockTomTomMap
import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.map.display.marker.MarkerOptions
import com.tomtom.sdk.map.display.TomTomMap
import com.tomtom.sdk.search.model.result.SearchResult
import com.example.roadtripbuddy.PlanATripViewModel
import com.example.roadtripbuddy.mocks.MockLocationService
import com.example.roadtripbuddy.PlanActivity
import kotlinx.coroutines.flow.MutableStateFlow
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class MapFunctionalityTest {

    private lateinit var mockPlanATripViewModel: PlanATripViewModel
    private lateinit var mockTomTomMap: TomTomMap
    private lateinit var mockSearchResult: SearchResult
    private lateinit var mockContext: Context
    private lateinit var mockActivity: PlanActivity
    private lateinit var planMap: PlanMap

    @Before
    fun setup() {
        mockPlanATripViewModel = MockPlanATripViewModel.instance
        mockTomTomMap = MockTomTomMap().mock
        mockSearchResult = mockk {
            every { place } returns mockk {
                every { coordinate } returns GeoPoint(0.0, 0.0)
                every { address } returns mockk {
                    every { freeformAddress } returns "Test Address"
                }
            }
        }
        mockContext = mockk<Context>(relaxed = true) {
            every { getPackageName() } returns "com.example.roadtripbuddy"
            every { getCacheDir() } returns File("/tmp/cache")
        }
        mockActivity = mockk<PlanActivity>(relaxed = true)
        planMap = PlanMap(
            context = mockContext,
            activity = mockActivity,
            mapReadyState = mutableStateOf(true),
            locationService = MockLocationService.instance
        )
        // Set tomTomMap manually to avoid composable
        planMap.javaClass.getDeclaredField("tomTomMap").apply {
            isAccessible = true
            set(planMap, mockTomTomMap)
        }
    }

    @Test
    fun map_showsMarkers_basedOnWaypoints() {
        val waypoints = listOf(WaypointItem(searchResult = mockSearchResult, hour = 0, minute = 0))
        every { mockPlanATripViewModel.planWaypoints } returns MutableStateFlow(waypoints)

        // Call non-composable method to add marker
        planMap.planATripCameraInit(mockSearchResult)

        verify {
            mockTomTomMap.addMarker(
                MarkerOptions(
                    coordinate = GeoPoint(0.0, 0.0),
                    pinImage = any()
                )
            )
        }
    }
}