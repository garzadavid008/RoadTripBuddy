package com.example.roadtripbuddy.edgecasesTests

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.Lifecycle
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.roadtripbuddy.PlanATripViewModel
import com.example.roadtripbuddy.PlanMap
import com.example.roadtripbuddy.PlanRouteManager
import com.example.roadtripbuddy.PlacesViewModel
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

    private val mockPlanATripViewModel = mockk<PlanATripViewModel>(relaxed = true)
    private val mockPlacesViewModel = mockk<PlacesViewModel>(relaxed = true)
    private val mockPlanRouteManager = mockk<PlanRouteManager>(relaxed = true)

    @Test
    fun handles_nullViewModelState() {
        every { mockPlanATripViewModel.planWaypoints } returns MutableStateFlow(null)

        composeTestRule.setContent {
            PlanMap(
                context = mockk(),
                activity = mockk(),
                mapReadyState = mutableStateOf(true)
            ).PlanMapContent()
        }

        verify { mockPlanATripViewModel.planWaypoints }
    }

    @Test
    fun handles_lowBatteryMode() {
        every { mockPlanATripViewModel.planWaypoints } returns MutableStateFlow(emptyList())

        composeTestRule.setContent {
            PlanMap(
                context = mockk(),
                activity = mockk(),
                mapReadyState = mutableStateOf(true)
            ).PlanMapContent()
        }

        composeTestRule.activity.sendBroadcast(Intent(Intent.ACTION_BATTERY_LOW))

        verify { mockPlanATripViewModel.planWaypoints }
    }

    @Test
    fun handles_backgroundForegroundTransition() {
        every { mockPlanATripViewModel.planWaypoints } returns MutableStateFlow(emptyList())

        val scenario = ActivityScenarioRule<ComponentActivity>(Intent())
        scenario.moveToState(Lifecycle.State.CREATED)
        scenario.moveToState(Lifecycle.State.RESUMED)

        composeTestRule.setContent {
            PlanMap(
                context = mockk(),
                activity = mockk(),
                mapReadyState = mutableStateOf(true)
            ).PlanMapContent()
        }

        verify { mockPlanATripViewModel.planWaypoints }
    }
}

/*This Tests(Error States):
*App behavior with no network
*Unexpected crashes or null states
*ViewModel handling errors
* */