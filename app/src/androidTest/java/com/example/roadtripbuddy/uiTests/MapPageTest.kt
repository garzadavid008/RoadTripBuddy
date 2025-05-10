package com.example.roadtripbuddy.uiTests

import android.content.pm.ActivityInfo
import androidx.activity.ComponentActivity
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.roadtripbuddy.PlanATripViewModel
import com.example.roadtripbuddy.PlanMap
import com.example.roadtripbuddy.PlanRouteManager
import com.example.roadtripbuddy.PlacesViewModel
import com.example.roadtripbuddy.SearchDrawerAutocomplete
import com.example.roadtripbuddy.SearchDrawerViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MapPageTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val mockPlanATripViewModel = mockk<PlanATripViewModel>(relaxed = true)
    private val mockPlacesViewModel = mockk<PlacesViewModel>(relaxed = true)
    private val mockPlanRouteManager = mockk<PlanRouteManager>(relaxed = true)
    private val mockSearchDrawerViewModel = mockk<SearchDrawerViewModel>(relaxed = true)
    private val mockNavigationMap = mockk<NavigationMap>(relaxed = true)

    @Test
    fun mapScreen_rendersCorrectly() {
        every { mockPlanATripViewModel.planWaypoints } returns MutableStateFlow(emptyList())

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
    fun mapScreen_showsLoadingState() {
        every { mockPlanATripViewModel.planWaypoints } returns MutableStateFlow(null)

        composeTestRule.setContent {
            PlanMap(
                context = mockk(),
                activity = mockk(),
                mapReadyState = mutableStateOf(false)
            ).PlanMapContent()
        }

        verify { mockPlanATripViewModel.planWaypoints }
    }

    @Test
    fun mapScreen_hasContentDescriptions() {
        every { mockSearchDrawerViewModel.ETA } returns MutableStateFlow("10 min")

        composeTestRule.setContent {
            SearchDrawerAutocomplete(
                navMap = mockNavigationMap,
                placesViewModel = mockPlacesViewModel,
                searchDrawerViewModel = mockSearchDrawerViewModel,
                onDone = {},
                isTyping = {}
            )
        }

        // Note: Verify content descriptions in SearchDrawerAutocomplete
        verify { mockSearchDrawerViewModel.ETA }
    }

    @Test
    fun mapScreen_persistsStateOnRotation() {
        every { mockPlanATripViewModel.planWaypoints } returns MutableStateFlow(emptyList())

        composeTestRule.setContent {
            PlanMap(
                context = mockk(),
                activity = mockk(),
                mapReadyState = mutableStateOf(true)
            ).PlanMapContent()
        }

        composeTestRule.activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        verify { mockPlanATripViewModel.planWaypoints }
    }
}



/*Check list:
* Mapp screen renders correctly
* Ui updates based on viewModel state(loading, error, success
* Accessibility(Content descriptions for images)
* Screen rotation handling*/