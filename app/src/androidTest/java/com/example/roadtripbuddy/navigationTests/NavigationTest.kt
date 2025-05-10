package com.example.roadtripbuddy.navigationTests

import android.content.Intent
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.roadtripbuddy.MainActivity
import com.example.roadtripbuddy.NavigationMap
import com.example.roadtripbuddy.PlacesViewModel
import com.example.roadtripbuddy.SearchDrawerAutocomplete
import com.example.roadtripbuddy.SearchDrawerViewModel
import com.example.roadtripbuddy.mocks.MockAuthViewModel
import com.example.roadtripbuddy.pages.LoginPage
import com.example.roadtripbuddy.pages.Suggestions
import io.mockk.mockk
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NavigationTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private lateinit var navController: TestNavHostController

    @Before
    fun setup() {
        navController = TestNavHostController(ApplicationProvider.getApplicationContext())
    }

    @Test
    fun navigates_fromLoginToMap() {
        composeTestRule.setContent {
            NavHost(navController, startDestination = "login") {
                composable("login") { LoginPage(navController, MockAuthViewModel()) }
                composable("map") { }
            }
        }

        composeTestRule.onNodeWithTag("submit_button").performClick()
        assertEquals("map", navController.currentDestination?.route)
    }

    @Test
    fun navigates_toSuggestions() {
        composeTestRule.setContent {
            NavHost(navController, startDestination = "map") {
                composable("map") { }
                composable("suggestion") { Suggestions(navController, mockk()) }
            }
        }

        navController.navigate("suggestion")
        assertEquals("suggestion", navController.currentDestination?.route)
    }

    @Test
    fun navigates_toSearchDrawer() {
        val mockSearchDrawerViewModel = mockk<SearchDrawerViewModel>(relaxed = true)
        val mockPlacesViewModel = mockk<PlacesViewModel>(relaxed = true)
        val mockNavigationMap = mockk<NavigationMap>(relaxed = true)

        composeTestRule.setContent {
            NavHost(navController, startDestination = "map") {
                composable("map") { }
                composable("search") {
                    SearchDrawerAutocomplete(
                        navMap = mockNavigationMap,
                        placesViewModel = mockPlacesViewModel,
                        searchDrawerViewModel = mockSearchDrawerViewModel,
                        onDone = {},
                        isTyping = {}
                    )
                }
            }
        }

        navController.navigate("search")
        assertEquals("search", navController.currentDestination?.route)
    }

    @Test
    fun handles_deepLink_toMap() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("roadtripbuddy://map/route123"))
        val scenario = ActivityScenarioRule<MainActivity>(intent)
        scenario.onActivity {
            assertEquals("map", navController.currentDestination?.route)
        }
    }
}

/*Checklist:
* Navigation between all main flows(login -> map and so on
*deep link handling
* */