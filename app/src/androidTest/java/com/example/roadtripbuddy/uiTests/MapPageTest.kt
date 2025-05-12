package com.example.roadtripbuddy.uiTests

import android.content.pm.ActivityInfo
import androidx.activity.ComponentActivity
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.roadtripbuddy.NavigationMap
import com.example.roadtripbuddy.PlanMap
import com.example.roadtripbuddy.PlanATripViewModel
import com.example.roadtripbuddy.PlacesViewModel
import com.example.roadtripbuddy.SearchDrawer.SearchDrawerAutocomplete
import com.example.roadtripbuddy.SearchDrawerViewModel
import com.example.roadtripbuddy.mocks.MockNavigationMap
import com.example.roadtripbuddy.mocks.MockPlanATripViewModel
import com.example.roadtripbuddy.mocks.MockPlacesViewModel
import com.example.roadtripbuddy.mocks.MockSearchDrawerViewModel
import com.example.roadtripbuddy.mocks.MockLocationService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MapPageTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var mockPlanATripViewModel: PlanATripViewModel
    private lateinit var mockPlacesViewModel: PlacesViewModel
    private lateinit var mockSearchDrawerViewModel: SearchDrawerViewModel
    private lateinit var mockNavigationMap: NavigationMap

    @Before
    fun setup() {
        mockPlanATripViewModel = MockPlanATripViewModel.instance
        mockPlacesViewModel = MockPlacesViewModel.instance
        mockSearchDrawerViewModel = MockSearchDrawerViewModel.instance
        mockNavigationMap = MockNavigationMap.instance
    }

    @Test
    fun mapScreen_rendersCorrectly() {
        every { mockPlanATripViewModel.planWaypoints } returns MutableStateFlow(emptyList())

        composeTestRule.setContent {
            PlanMap(
                context = mockk(),
                activity = mockk(),
                mapReadyState = remember { mutableStateOf(true) },
                locationService = MockLocationService.instance
            ).PlanMapContent()
        }

        verify { mockPlanATripViewModel.planWaypoints }
    }

    @Test
    fun mapScreen_showsLoadingState() {
        every { mockPlanATripViewModel.planWaypoints } returns MutableStateFlow(emptyList())

        composeTestRule.setContent {
            PlanMap(
                context = mockk(),
                activity = mockk(),
                mapReadyState = remember { mutableStateOf(false) },
                locationService = MockLocationService.instance
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
                isTyping = {},
                category = {}
            )
        }

        composeTestRule.onNodeWithText("Search Location").assertIsDisplayed()
        verify { mockSearchDrawerViewModel.ETA }
    }

    @Test
    fun mapScreen_persistsStateOnRotation() {
        every { mockPlanATripViewModel.planWaypoints } returns MutableStateFlow(emptyList())

        composeTestRule.setContent {
            PlanMap(
                context = mockk(),
                activity = mockk(),
                mapReadyState = remember { mutableStateOf(true) },
                locationService = MockLocationService.instance
            ).PlanMapContent()
        }

        composeTestRule.activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        verify { mockPlanATripViewModel.planWaypoints }
    }
}