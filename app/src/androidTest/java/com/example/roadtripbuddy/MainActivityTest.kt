import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.navigation.testing.TestNavHostController
import androidx.test.core.app.ApplicationProvider
import com.example.roadtripbuddy.MainActivity
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class MainActivityTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private lateinit var navController: TestNavHostController

    @Before
    fun setup() {
        navController = TestNavHostController(ApplicationProvider.getApplicationContext())
    }

    @Test
    fun app_startsAtMapScreen() {
        composeTestRule.setContent {
            MainActivity()
        }

        // Verify if "map" screen is the starting destination
        assert(navController.currentDestination?.route == "map")
    }
}
