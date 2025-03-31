import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import com.example.roadtripbuddy.pages.LoginPage
import com.example.roadtripbuddy.AuthViewModel
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AuthenticationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var navController: TestNavHostController
    private lateinit var authViewModel: AuthViewModel

    @Before
    fun setup() {
        navController = TestNavHostController(ApplicationProvider.getApplicationContext())
        authViewModel = AuthViewModel()
    }

    @Test
    fun loginUser_redirectsToMap() {
        composeTestRule.setContent {
            LoginPage(navController, authViewModel)
        }

        // Enter email
        composeTestRule.onNodeWithText("Email").performTextInput("test@example.com")

        // Enter password
        composeTestRule.onNodeWithText("Password").performTextInput("password123")

        // Click login
        composeTestRule.onNodeWithText("Submit").performClick()

        // Verify navigation to the map screen
        composeTestRule.waitForIdle()
    }
}


