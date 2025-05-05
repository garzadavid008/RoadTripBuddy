package com.example.roadtripbuddy.authenticationTests // Groups tests under authenticationTests

import androidx.compose.ui.test.* // Contains performClick, onNodeWithText, performTextInput
//import androidx.compose.ui.test.junit4.createAndroidComposeRule  // ws removed for customTest
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
//import androidx.lifecycle.compose.LocalViewModelStoreOwner
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.ComposeNavigator
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.roadtripbuddy.pages.LoginPage
import com.example.roadtripbuddy.pages.SignupPage
import com.example.roadtripbuddy.IAuthViewModel
import com.example.roadtripbuddy.mocks.MockAuthViewModel
//import com.example.roadtripbuddy.MainActivity
//import com.example.roadtripbuddy.test.TestActivity
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.assertEquals

@RunWith(AndroidJUnit4::class)
class AuthNavigationTest {
    // Set ups composing test environment to run and verify Compose UI components.
    @get:Rule

    val composeTestRule = createComposeRule()

    // Initialize test nav controller before each test.
    private lateinit var navController: TestNavHostController
    private lateinit var authViewModel: IAuthViewModel

    @Before
    fun setup() {
        navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        navController.navigatorProvider.addNavigator(ComposeNavigator())

        authViewModel = MockAuthViewModel(loginSuccessful = true, signupSuccessful = true)
    }

    // Test to verify after mock login, nav to map functions.
    @Test
    fun navigate_fromLoginToMap() {
        // Use the activity's default NavHost, ensure it starts at login
        composeTestRule.setContent {
            navController.setLifecycleOwner(LocalLifecycleOwner.current)
            NavHost(navController = navController, startDestination = "login") {
                composable("login") { LoginPage(navController, authViewModel) }
                composable("map") { }
            }
        }
        // Simulate user interaction
        composeTestRule.onNodeWithTag("email_field").performTextInput("test@example.com")
        composeTestRule.onNodeWithTag("password_field").performTextInput("password123")
        composeTestRule.onNodeWithTag("submit_button").performClick()

        // Wait for navigation
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            navController.currentDestination?.route == "map"
        }
        // Assert navigation
        assertEquals("map", navController.currentDestination?.route)
    }

    @Test
    fun navigate_fromSignupToMap() {
        composeTestRule.setContent {
            navController.setLifecycleOwner(LocalLifecycleOwner.current)
            NavHost(navController = navController, startDestination = "signup") {
                composable("signup") { SignupPage(navController, authViewModel) }
                composable("map") { }
            }
        }

        composeTestRule.onNodeWithTag("name_field").performTextInput("John Doe")
        composeTestRule.onNodeWithTag("vehicle_field").performTextInput("SUV")
        composeTestRule.onNodeWithTag("email_field").performTextInput("test@example.com")
        composeTestRule.onNodeWithTag("password_field").performTextInput("Password123!")
        composeTestRule.onNodeWithTag("confirm_password_field").performTextInput("Password123!")
        composeTestRule.onNodeWithTag("submit_button").performClick()

        // Wait for navigation
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            navController.currentDestination?.route == "map"
        }
        assertEquals("map", navController.currentDestination?.route)
    }

    @Test
    fun navigate_fromLoginToSignup() {
        composeTestRule.setContent {
            navController.setLifecycleOwner(LocalLifecycleOwner.current)
            NavHost(navController = navController, startDestination = "login") {
                composable("login") { LoginPage(navController, authViewModel) }
                composable("signup") { SignupPage(navController, authViewModel) }
            }
        }

        composeTestRule.onNodeWithText("Don't have an account? Sign up here").performClick()
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            navController.currentDestination?.route == "signup"
        }
        assertEquals("signup", navController.currentDestination?.route)
    }

    // Test to verify after mock signup, nav to map functions
    @Test
    fun navigate_fromSignupToLogin() {
        composeTestRule.setContent {
            navController.setLifecycleOwner(LocalLifecycleOwner.current)
            NavHost(navController = navController, startDestination = "signup") {
                composable("signup") { SignupPage(navController, authViewModel) }
                composable("login") { LoginPage(navController, authViewModel) }
            }
        }
        // Simulate user interaction
        // Click the "Already have an account, Login!" button, Removed everything else since it triggered a signup after a successful nav to map
        composeTestRule.onNodeWithText("Already have an account, Login!").performClick()

        // Wait for navigation
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            navController.currentDestination?.route == "login"
        }
        composeTestRule.waitForIdle()// Assert navigation
        assertEquals("login", navController.currentDestination?.route)

    }
}

/*
* if it goes from signup-> login -> map
* or signup -> map
* Untested Scenarios not listed as of yet, list currently pending if need to add anything else
* */

/* This test should cover:
* User interactions sims filling fields & clicking submit
* waitUntil, which should have the program wait for navigation to complete, increasing stability(plz)
* added a timeout uses 5000ms timeout to handle any delays in authState updates or nav (can edit if necessary)
* Navigation after login  || Login -> map
* Navigation after signup || Signup -> login
* Note:
* Ui elements, user interactions(clicking,..), nav outcomes, and error handling
* should both be in login and signup tests.*/