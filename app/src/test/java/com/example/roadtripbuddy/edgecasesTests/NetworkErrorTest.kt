/*package com.example.roadtripbuddy.edgecasesTests

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import androidx.activity.ComponentActivity
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import com.example.roadtripbuddy.PlanATripViewModel
import com.example.roadtripbuddy.PlanActivity
import com.example.roadtripbuddy.PlanMap
import com.example.roadtripbuddy.PlanRouteManager
import com.example.roadtripbuddy.WaypointItem
import com.example.roadtripbuddy.mocks.MockLocationService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NetworkErrorTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_COARSE_LOCATION
    )

    private val mockPlanATripViewModel = mockk<PlanATripViewModel>(relaxed = true)
    private val mockPlanRouteManager = mockk<PlanRouteManager>(relaxed = true)

    @Test
    fun handles_nullViewModelState() {
        every { mockPlanATripViewModel.planWaypoints } returns MutableStateFlow<List<WaypointItem>>(emptyList())
        every { mockPlanRouteManager.planOnRouteRequest(any(), any(), any()) } returns Unit

        composeTestRule.setContent {
            val mockContext = mockk<Context>()
            val mockResources = mockk<Resources>()
            every { mockContext.resources } returns mockResources
            every { mockResources.getIdentifier(any(), "drawable", any()) } returns 12345
            every { mockResources.getIdentifier("line_pointer", "drawable", any()) } returns 67890
            every { mockResources.getIdentifier("ic_marker_1", "drawable", any()) } returns 12346
            every { mockResources.getIdentifier("ic_marker_2", "drawable", any()) } returns 12347

            PlanMap(
                context = mockContext,
                activity = mockk<PlanActivity>(),
                mapReadyState = mutableStateOf(true),
                locationService = MockLocationService.instance
            ).PlanMapContent()
        }

        verify { mockPlanATripViewModel.planWaypoints }
    }

    @Test
    fun handles_lowBatteryMode() {
        every { mockPlanATripViewModel.planWaypoints } returns MutableStateFlow<List<WaypointItem>>(emptyList())
        every { mockPlanRouteManager.planOnRouteRequest(any(), any(), any()) } returns Unit

        composeTestRule.setContent {
            val mockContext = mockk<Context>()
            val mockResources = mockk<Resources>()
            every { mockContext.resources } returns mockResources
            every { mockResources.getIdentifier(any(), "drawable", any()) } returns 12345
            every { mockResources.getIdentifier("line_pointer", "drawable", any()) } returns 67890
            every { mockResources.getIdentifier("ic_marker_1", "drawable", any()) } returns 12346
            every { mockResources.getIdentifier("ic_marker_2", "drawable", any()) } returns 12347

            PlanMap(
                context = mockContext,
                activity = mockk<PlanActivity>(),
                mapReadyState = mutableStateOf(true),
                locationService = MockLocationService.instance
            ).PlanMapContent()
        }

        composeTestRule.activity.sendBroadcast(Intent(Intent.ACTION_BATTERY_LOW))

        verify { mockPlanATripViewModel.planWaypoints }
    }

    @Test
    fun handles_networkError() {
        every { mockPlanATripViewModel.planWaypoints } returns MutableStateFlow<List<WaypointItem>>(emptyList())
        every { mockPlanRouteManager.planOnRouteRequest(any(), any(), any()) } throws RuntimeException("Network error")

        composeTestRule.setContent {
            val mockContext = mockk<Context>()
            val mockResources = mockk<Resources>()
            every { mockContext.resources } returns mockResources
            every { mockResources.getIdentifier(any(), "drawable", any()) } returns 12345
            every { mockResources.getIdentifier("line_pointer", "drawable", any()) } returns 67890
            every { mockResources.getIdentifier("ic_marker_1", "drawable", any()) } returns 12346
            every { mockResources.getIdentifier("ic_marker_2", "drawable", any()) } returns 12347

            PlanMap(
                context = mockContext,
                activity = mockk<PlanActivity>(),
                mapReadyState = mutableStateOf(true),
                locationService = MockLocationService.instance
            ).PlanMapContent()
        }

        verify { mockPlanATripViewModel.planWaypoints }
    }
}*/

/*Deprecated
* Not enough time*/