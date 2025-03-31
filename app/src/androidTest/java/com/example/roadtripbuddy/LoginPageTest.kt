import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.NavController
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import com.example.roadtripbuddy.pages.LoginPage
import com.example.roadtripbuddy.AuthViewModel
import com.example.roadtripbuddy.AuthState
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class LoginPageTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var navController: NavController
    private lateinit var authViewModel: AuthViewModel

    @Before
    fun setup() {
        navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        authViewModel = AuthViewModel()
    }

    @Test
    fun loginPage_displaysAllComponents() {
        composeTestRule.setContent {
            LoginPage(navController, authViewModel)
        }

        // Check if email field is displayed
        composeTestRule.onNodeWithText("Email").assertExists()

        // Check if password field is displayed
        composeTestRule.onNodeWithText("Password").assertExists()

        // Check if submit button exists
        composeTestRule.onNodeWithText("Submit").assertExists()
    }

    @Test
    fun loginButton_click_triggersAuthViewModel() {
        composeTestRule.setContent {
            LoginPage(navController, authViewModel)
        }

        composeTestRule.onNodeWithText("Email").performTextInput("test@example.com")
        composeTestRule.onNodeWithText("Password").performTextInput("password123")
        composeTestRule.onNodeWithText("Submit").performClick()

        // Verify if AuthViewModel state changes
        composeTestRule.waitForIdle()
        assert(authViewModel.authState.value!! is AuthState.Loading)
    }
}
