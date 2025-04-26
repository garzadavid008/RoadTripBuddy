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
import com.example.roadtripbuddy.pages.SignupPage
import com.example.roadtripbuddy.mocks.MockAuthViewModel
//import com.example.roadtripbuddy.MainActivity
//import com.example.roadtripbuddy.test.TestActivity
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.assertEquals

@RunWith(AndroidJUnit4::class)
class SignUpTest {
    @get:Rule

    val composeTestRule = createComposeRule()
    //val composeTestRule = createAndroidComposeRule<MainActivity>()
    //val composeTestRule = createAndroidComposeRule<TestActivity>()

    private lateinit var navController: TestNavHostController
    private lateinit var authViewModel: IAuthViewModel

    // navController is used to simulate and monitor navigation.
    // authViewModel is mocked to simulate success/fail authentication
    @Before
    fun setup() {
        navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        navController.navigatorProvider.addNavigator(ComposeNavigator())
        //composeTestRule.setContent {
        //    navController.setLifecycleOwner(LocalLifecycleOwner.current)
           // navController.setViewModelStore(LocalViewModelStoreOwner.current!!.viewModelStore)
      //  }
        //navController.navigatorProvider.addNavigator(ComposeNavigator())
        authViewModel = MockAuthViewModel(loginSuccessful = true, signupSuccessful = true)
    }

    // Tests if all UI elements in SignUp page exists.
    // Elements: Name, Vehicle, Email, Password, Confirm, and Submit
    @Test
    fun signupPage_displaysAllComponents() {
        composeTestRule.setContent {
            navController.setLifecycleOwner(LocalLifecycleOwner.current)
            SignupPage(navController = navController, authViewModel = authViewModel)
        }
        composeTestRule.onNodeWithTag("name_field").assertExists()
        composeTestRule.onNodeWithTag("vehicle_field").assertExists()
        composeTestRule.onNodeWithTag("email_field").assertExists()
        composeTestRule.onNodeWithTag("password_field").assertExists()
        composeTestRule.onNodeWithTag("confirm_password_field").assertExists()
        composeTestRule.onNodeWithTag("submit_button").assertExists()
        composeTestRule.onNodeWithText("Already have an account, Login!").assertExists() // Added space(not having space cause a test to fail for several days):<
        composeTestRule.onNodeWithText("Back to Map").assertExists()
    }

    // Simulates valid signup and checks that app nav to map screen.
    @Test
    fun signupButton_click_triggersAuthViewModel() {
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
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            navController.currentDestination?.route == "map"
        }
        assertEquals("map", navController.currentDestination?.route)
    }

    // Tests that pressing Signup(Or whatever we have there) with empty field(s) should:
    // show error message, remain in Signup screen
    @Test
    fun signupButton_emptyInput_showsError() {
        authViewModel = MockAuthViewModel(signupSuccessful = false)
        composeTestRule.setContent {
            navController.setLifecycleOwner(LocalLifecycleOwner.current)
            NavHost(navController = navController, startDestination = "signup") {
                composable("signup") { SignupPage(navController, authViewModel) }
            }
        }
        composeTestRule.onNodeWithTag("submit_button").performClick()
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithTag("error_message").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithTag("error_message").assertTextEquals("All fields are required")
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            navController.currentDestination?.route != null
        }
        assertEquals("signup", navController.currentDestination?.route)
    }

    // Simulates a weak password preventing navigation, should stay on Signup
    @Test
    fun signupButton_invalidPassword_showsError() {
        authViewModel = MockAuthViewModel(signupSuccessful = false)
        composeTestRule.setContent {
            navController.setLifecycleOwner(LocalLifecycleOwner.current)
            NavHost(navController = navController, startDestination = "signup") {
                composable("signup") { SignupPage(navController, authViewModel) }
            }
        }
        composeTestRule.onNodeWithTag("name_field").performTextInput("John Doe")
        composeTestRule.onNodeWithTag("vehicle_field").performTextInput("SUV")
        composeTestRule.onNodeWithTag("email_field").performTextInput("test@example.com")
        composeTestRule.onNodeWithTag("password_field").performTextInput("weak")
        composeTestRule.onNodeWithTag("confirm_password_field").performTextInput("weak")
        composeTestRule.onNodeWithTag("submit_button").performClick()
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithTag("error_message").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithTag("error_message").assertTextEquals("Password is invalid")
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            navController.currentDestination?.route != null
        }
        assertEquals("signup", navController.currentDestination?.route)
    }

    // Simulates mismatch password preventing navigation, should stay on Signup
    @Test
    fun signupButton_mismatchedPasswords_showsError() {
        authViewModel = MockAuthViewModel(signupSuccessful = false)
        composeTestRule.setContent {
            navController.setLifecycleOwner(LocalLifecycleOwner.current)
            NavHost(navController = navController, startDestination = "signup") {
                composable("signup") { SignupPage(navController, authViewModel) }
            }
        }
        composeTestRule.onNodeWithTag("name_field").performTextInput("John Doe")
        composeTestRule.onNodeWithTag("vehicle_field").performTextInput("SUV")
        composeTestRule.onNodeWithTag("email_field").performTextInput("test@example.com")
        composeTestRule.onNodeWithTag("password_field").performTextInput("Password123!")
        composeTestRule.onNodeWithTag("confirm_password_field").performTextInput("Different123!")
        composeTestRule.onNodeWithTag("submit_button").performClick()
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithTag("error_message").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithTag("error_message").assertTextEquals("Passwords do not match")
        assertEquals("signup", navController.currentDestination?.route)
    }

    // Test if entered vehicle is invalid, Added the validation in SignupPage
    @Test
    fun signupButton_invalidVehicleType_showsError() {
        authViewModel = MockAuthViewModel(signupSuccessful = false)
        composeTestRule.setContent {
            navController.setLifecycleOwner(LocalLifecycleOwner.current)
            NavHost(navController = navController, startDestination = "signup") {
                composable("signup") { SignupPage(navController, authViewModel) }
            }
        }
        composeTestRule.onNodeWithTag("name_field").performTextInput("John Doe")
        composeTestRule.onNodeWithTag("vehicle_field").performTextInput("Invalid")
        composeTestRule.onNodeWithTag("email_field").performTextInput("test@example.com")
        composeTestRule.onNodeWithTag("password_field").performTextInput("Password123!")
        composeTestRule.onNodeWithTag("confirm_password_field").performTextInput("Password123!")
        composeTestRule.onNodeWithTag("submit_button").performClick()
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithTag("error_message").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithTag("error_message").assertTextEquals("Invalid vehicle type")
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            navController.currentDestination?.route != null
        }
        assertEquals("signup", navController.currentDestination?.route)
    }
    // To ensure that name arguments work,
    @Test
    fun signup_withNamedArguments_succeeds() {
        authViewModel = MockAuthViewModel(signupSuccessful = true)
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
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            navController.currentDestination?.route == "map"
        }
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            navController.currentDestination?.route != null
        }
        assertEquals("map", navController.currentDestination?.route)
    }

    // Test for a valid but minimal password
    @Test
    fun signupButton_minimalValidPassword_succeeds() {
        authViewModel = MockAuthViewModel(signupSuccessful = true)
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
        composeTestRule.onNodeWithTag("password_field").performTextInput("Pass123!a")
        composeTestRule.onNodeWithTag("confirm_password_field").performTextInput("Pass123!a")
        composeTestRule.onNodeWithTag("submit_button").performClick()
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            navController.currentDestination?.route == "map"
        }
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            navController.currentDestination?.route != null
        }
        assertEquals("map", navController.currentDestination?.route)
    }

    // FirebaseAuth Signup behaviour simulated by enhancing MockAuthViewModel(when I get to it)
    @Test
    fun signupButton_firebaseAuthFailure_showsError() {
        authViewModel = MockAuthViewModel(signupSuccessful = false, signupError = "Firebase: Email already in use")
        composeTestRule.setContent {
            navController.setLifecycleOwner(LocalLifecycleOwner.current)
            NavHost(navController = navController, startDestination = "signup") {
                composable("signup") { SignupPage(navController, authViewModel) }
            }
        }
        composeTestRule.onNodeWithTag("name_field").performTextInput("John Doe")
        composeTestRule.onNodeWithTag("vehicle_field").performTextInput("SUV")
        composeTestRule.onNodeWithTag("email_field").performTextInput("existing@example.com")
        composeTestRule.onNodeWithTag("password_field").performTextInput("Password123!")
        composeTestRule.onNodeWithTag("confirm_password_field").performTextInput("Password123!")
        composeTestRule.onNodeWithTag("submit_button").performClick()
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithTag("error_message").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithTag("error_message").assertTextEquals("Firebase: Email already in use")
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            navController.currentDestination?.route != null
        }
        assertEquals("signup", navController.currentDestination?.route)
    }

    // Network error test.
    @Test
    fun signupButton_networkError_showsError() {
        authViewModel = MockAuthViewModel(signupSuccessful = false, signupError = "Network error")
        composeTestRule.setContent {
            navController.setLifecycleOwner(LocalLifecycleOwner.current)
            NavHost(navController = navController, startDestination = "signup") {
                composable("signup") { SignupPage(navController, authViewModel) }
            }
        }
        composeTestRule.onNodeWithTag("name_field").performTextInput("John Doe")
        composeTestRule.onNodeWithTag("vehicle_field").performTextInput("SUV")
        composeTestRule.onNodeWithTag("email_field").performTextInput("test@example.com")
        composeTestRule.onNodeWithTag("password_field").performTextInput("Password123!")
        composeTestRule.onNodeWithTag("confirm_password_field").performTextInput("Password123!")
        composeTestRule.onNodeWithTag("submit_button").performClick()
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodesWithTag("error_message").fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithTag("error_message").assertTextEquals("Network error")
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            navController.currentDestination?.route != null
        }
        assertEquals("signup", navController.currentDestination?.route)
    }

}

/* This test should cover for Signup:
UI Rendering (Email, password, vehicle, confirmation Submit)
For empty/invalid input ensure app stay on signup screen
Navigation after valid signup.
FirebaseAuth signup failure (partial coverage via mock)
Error message for invalid signup ensure app stay on signup screen
Network error
*/

//Note:switched to createComposeRule() to test SignupPage in isolation to avoid MainActivity conflix