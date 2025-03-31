import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.example.roadtripbuddy.AuthViewModel
import com.example.roadtripbuddy.AuthState
import com.google.firebase.auth.FirebaseAuth
import io.mockk.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AuthViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule() // Allows LiveData to work synchronously in tests

    private lateinit var authViewModel: AuthViewModel
    private val firebaseAuth: FirebaseAuth = mockk(relaxed = true) // Mock Firebase Auth

    @Before
    fun setup() {
        authViewModel = AuthViewModel()
    }

    @Test
    fun `checkAuth should set Unauthenticated when user is null`() {
        every { firebaseAuth.currentUser } returns null  // Pretend Firebase user is NULL

        val observer = mockk<Observer<AuthState>>(relaxed = true) // Observe changes
        authViewModel.authState.observeForever(observer)

        authViewModel.checkAuth()

        verify { observer.onChanged(AuthState.Unauthenticated) } // Expect Unauthenticated
    }

    @Test
    fun `checkAuth should set Authenticated when user exists`() {
        every { firebaseAuth.currentUser } returns mockk()  // Pretend Firebase user exists

        val observer = mockk<Observer<AuthState>>(relaxed = true)
        authViewModel.authState.observeForever(observer)

        authViewModel.checkAuth()

        verify { observer.onChanged(AuthState.Authenticated) } // Expect Authenticated
    }
}