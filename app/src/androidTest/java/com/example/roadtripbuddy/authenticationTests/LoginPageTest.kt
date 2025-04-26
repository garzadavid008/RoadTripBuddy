package com.example.roadtripbuddy.authenticationTests

import androidx.compose.ui.test.*
//import androidx.compose.ui.test.junit4.createAndroidComposeRule
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
//import com.example.roadtripbuddy.AuthState
import com.example.roadtripbuddy.IAuthViewModel
import com.example.roadtripbuddy.pages.LoginPage
import com.example.roadtripbuddy.mocks.MockAuthViewModel
//import com.example.roadtripbuddy.MainActivity
//import com.example.roadtripbuddy.test.TestActivity
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse

@RunWith(AndroidJUnit4::class)
// Note: This file is for testing the UI of the LoginPage
class LoginPageTest {
    // This is the Compose test rule used to create and manage the UI test environment
    @get:Rule

    val composeTestRule = createComposeRule()
    //val composeTestRule = createAndroidComposeRule<MainActivity>()
    //val composeTestRule = createAndroidComposeRule<TestActivity>()

    private lateinit var navController: TestNavHostController
    private lateinit var authViewModel: IAuthViewModel

    // Set up test environment
    @Before
    fun setup() {
        // Sets up fake navController to tract navigation.
        navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        navController.navigatorProvider.addNavigator(ComposeNavigator())
        //composeTestRule.setContent {
           // navController.setLifecycleOwner(LocalLifecycleOwner.current)
           // navController.setViewModelStore(LocalViewModelStoreOwner.current!!.viewModelStore)
       // }
        // Sets up MockAuthViewModel into AuthViewModel to test whether login succeeded/fail.
        authViewModel = MockAuthViewModel(loginSuccessful = true)
    }

    // Tests if all UI elements in LoginUp page exists.
    // Elements: Email, Password, and Submit
    @Test
    fun loginPage_displaysAllComponents() {
        composeTestRule.setContent {
            navController.setLifecycleOwner(LocalLifecycleOwner.current)
            LoginPage(navController = navController, authViewModel = authViewModel)
        }
        // Check if email field is displayed
        composeTestRule.onNodeWithTag("email_field").assertExists()
        // Check if password field is displayed
        composeTestRule.onNodeWithTag("password_field").assertExists()
        // Check if submit button exists
        composeTestRule.onNodeWithTag("submit_button").assertExists()
        composeTestRule.onNodeWithText("Don't have an account? Sign up here").assertExists()
        composeTestRule.onNodeWithText("Back to Map").assertExists()
    }

    // Simulates valid login and checks that app nav to map screen.
    @Test
    fun loginButton_click_triggersAuthViewModel() {
        composeTestRule.setContent {
            navController.setLifecycleOwner(LocalLifecycleOwner.current)
            NavHost(navController = navController, startDestination = "login") {
                composable("login") { LoginPage(navController, authViewModel) }
                composable("map") { }
            }
        }
        composeTestRule.onNodeWithTag("email_field").performTextInput("test@example.com")
        composeTestRule.onNodeWithTag("password_field").performTextInput("password123")
        composeTestRule.onNodeWithTag("submit_button").performClick()
        // Verify if AuthViewModel state changes
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            navController.currentDestination?.route == "map"
        }
        assertEquals("map", navController.currentDestination?.route)
    }

    // Simulates Invalid login and should remain in login screen.
    @Test
    fun loginButton_click_failedLogin_showsError() {
        authViewModel = MockAuthViewModel(loginSuccessful = false, loginError = "Incorrect Username & Password")
        composeTestRule.setContent {
            navController.setLifecycleOwner(LocalLifecycleOwner.current)
            NavHost(navController = navController, startDestination = "login") {
                composable("login") { LoginPage(navController, authViewModel) }
            }
        }
        composeTestRule.onNodeWithTag("email_field").performTextInput("wrong@example.com")
        composeTestRule.onNodeWithTag("password_field").performTextInput("wrongPassword")
        composeTestRule.onNodeWithTag("submit_button").performClick()
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithTag("error_message").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithTag("error_message").assertTextEquals("Incorrect Username & Password")
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            navController.currentDestination?.route != null
        }
        assertEquals("login", navController.currentDestination?.route)
    }

    // Tests that pressing login with empty field(s) should:
    // show error message, remain in login screen
    @Test
    fun loginButton_emptyInput_showsError() {
        authViewModel = MockAuthViewModel(loginSuccessful = false)
        composeTestRule.setContent {
            navController.setLifecycleOwner(LocalLifecycleOwner.current)
            NavHost(navController = navController, startDestination = "login") {
                composable("login") { LoginPage(navController, authViewModel) }
            }
        }
        //composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("submit_button").performClick()
       // composeTestRule.waitForIdle()
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithTag("error_message").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithTag("error_message").assertTextEquals("Email or Password cannot be empty")
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            navController.currentDestination?.route != null
        }
        assertEquals("login", navController.currentDestination?.route)
    }

    // Tests that any invalid email format is caught and rejected before submitting.
    // Should remain in login page, (Needs to be added) show "invalid email format" message
    @Test
    fun loginButton_invalidEmailFormat_showsError() {
        authViewModel = MockAuthViewModel(loginSuccessful = false)
        composeTestRule.setContent {
            navController.setLifecycleOwner(LocalLifecycleOwner.current)
            NavHost(navController = navController, startDestination = "login") {
                composable("login") { LoginPage(navController, authViewModel) }
            }
        }
        //composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("email_field").performTextInput("invalid@")
        composeTestRule.onNodeWithTag("password_field").performTextInput("password123")
        composeTestRule.onNodeWithTag("submit_button").performClick()
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithTag("error_message").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithTag("error_message").assertTextEquals("Invalid email format")
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            navController.currentDestination?.route != null
        }
        assertEquals("login", navController.currentDestination?.route) // No navigation
    }

    // Test for complex(use of unique characters(?!@#$)) but valid email.
    @Test
    fun loginButton_complexValidEmail_succeeds() {
        authViewModel = MockAuthViewModel(loginSuccessful = true)
        composeTestRule.setContent {
            navController.setLifecycleOwner(LocalLifecycleOwner.current)
            NavHost(navController = navController, startDestination = "login") {
                composable("login") { LoginPage(navController, authViewModel) }
                composable("map") { }
            }
        }
       // composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("email_field").performTextInput("user.name+test@subdomain.example.com")
        composeTestRule.onNodeWithTag("password_field").performTextInput("password123")
        composeTestRule.onNodeWithTag("submit_button").performClick()
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            navController.currentDestination?.route == "map"
        }
        assertEquals("map", navController.currentDestination?.route)
    }


    @Test
    fun loginButton_click_backNavigationToLogin_preventsBack() {
        authViewModel = MockAuthViewModel(loginSuccessful = true)
        composeTestRule.setContent {
            navController.setLifecycleOwner(LocalLifecycleOwner.current)
            NavHost(navController = navController, startDestination = "login") {
                composable("login") { LoginPage(navController, authViewModel) }
                composable("map") { }
            }
        }
        composeTestRule.onNodeWithTag("email_field").performTextInput("test@example.com")
        composeTestRule.onNodeWithTag("password_field").performTextInput("password123")
        composeTestRule.onNodeWithTag("submit_button").performClick()
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            navController.currentDestination?.route == "map"
        }
        // Verify that the back stack contains only the map destination
        composeTestRule.runOnUiThread {
            // Check that login is not in the back stack
            val popped = navController.popBackStack("login", inclusive = false)
            assertFalse(popped) // login should not be found
        }
        assertEquals("map", navController.currentDestination?.route)
    }

    // FirebaseAuth Login behaviour simulated by enhancing MockAuthViewModel
    // Mock test for "User not found"
    @Test
    fun loginButton_firebaseAuthFailure_showsError() {
        authViewModel = MockAuthViewModel(loginSuccessful = false, loginError = "Firebase: User not found")
        composeTestRule.setContent {
            navController.setLifecycleOwner(LocalLifecycleOwner.current)
            NavHost(navController = navController, startDestination = "login") {
                composable("login") { LoginPage(navController, authViewModel) }
            }
        }
        //composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("email_field").performTextInput("nonexistent@example.com")
        composeTestRule.onNodeWithTag("password_field").performTextInput("password123")
        composeTestRule.onNodeWithTag("submit_button").performClick()
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithTag("error_message").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithTag("error_message").assertTextEquals("Firebase: User not found")
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            navController.currentDestination?.route != null
        }
        assertEquals("login", navController.currentDestination?.route)
    }

    // Mock test for "Account Disabled" (recall to view MockAuthViewModel to ensure this works)
    @Test
    fun loginButton_accountDisabled_showsError() {
        authViewModel = MockAuthViewModel(loginSuccessful = false, loginError = "Firebase: Account disabled")
        composeTestRule.setContent {
            navController.setLifecycleOwner(LocalLifecycleOwner.current)
            NavHost(navController = navController, startDestination = "login") {
                composable("login") { LoginPage(navController, authViewModel) }
            }
        }
        //composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("email_field").performTextInput("disabled@example.com")
        composeTestRule.onNodeWithTag("password_field").performTextInput("password123")
        composeTestRule.onNodeWithTag("submit_button").performClick()
        //composeTestRule.waitForIdle()
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithTag("error_message").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithTag("error_message").assertTextEquals("Firebase: Account disabled")
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            navController.currentDestination?.route != null
        }
        assertEquals("login", navController.currentDestination?.route)
    }

    // Network Error/Edge case
    @Test
    fun loginButton_networkError_showsError() {
        authViewModel = MockAuthViewModel(loginSuccessful = false, loginError = "Network error")
        composeTestRule.setContent {
            navController.setLifecycleOwner(LocalLifecycleOwner.current)
            NavHost(navController = navController, startDestination = "login") {
                composable("login") { LoginPage(navController, authViewModel) }
            }
        }
        //composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("email_field").performTextInput("test@example.com")
        composeTestRule.onNodeWithTag("password_field").performTextInput("password123")
        composeTestRule.onNodeWithTag("submit_button").performClick()
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithTag("error_message").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithTag("error_message").assertTextEquals("Network error")
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            navController.currentDestination?.route != null
        }
        assertEquals("login", navController.currentDestination?.route)
    }

}

// add tess that checks if pressing the back button after login exits the app
//or pops back to login but let me get these error's sorted out.

/* This test should cover for Login:
UI Rendering (Email, password, Submit)
For empty/invalid input ensure app stay on login screen
Navigation after valid login.
FirebaseAuth Login failure (partial coverage via mock)
Error message for invalid login ensure app stay on login screen
Network error
*/

//Note:switched to createComposeRule() to test LoginPage in isolation to avoid MainActivity conflix