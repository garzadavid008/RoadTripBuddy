package com.example.roadtripbuddy.viewmodelTests

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import com.example.roadtripbuddy.AuthState
import com.example.roadtripbuddy.AuthViewModel
import com.example.roadtripbuddy.IAuthViewModel
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import io.mockk.*
import org.junit.Before
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.assertEquals // previous import replaced with JUnit standard testing framework for android

class AuthViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule() // Allows LiveData to work synchronously in tests
    //val coroutineRule = MainCoroutineRule() // For coroutine testing
    private lateinit var authViewModel: IAuthViewModel // changed to IAuthViewModel
    private val firebaseAuth: FirebaseAuth = mockk(relaxed = true) // Mock Firebase Auth
    private val authStateObserver = mockk<Observer<AuthState>>(relaxed = true)
    //private val firestore: FirebaseFirestore = mockk(relaxed = true)
    //private val db: FirebaseFirestore = mockk(relaxed = true)

    @Before
    fun setup() {
        // enter mock FireBaseAuth and FirebaseFirestore into AuthViewModel.
        authViewModel = AuthViewModel(firebaseAuth)
        authViewModel.authState.observeForever(authStateObserver)
    }

    @After
    fun tearDown() {
        // Clean up mocks and observers
        authViewModel.authState.removeObserver(authStateObserver)
        clearAllMocks()
    }


    @Test
    fun `checkAuth should set Unauthenticated when user is null`() {
        every { firebaseAuth.currentUser } returns null  // mock Firebase user is NULL

        val observer = mockk<Observer<AuthState>>(relaxed = true) // Observe changes
        authViewModel.authState.observeForever(observer)

        authViewModel.checkAuth()

        verify { observer.onChanged(AuthState.Unauthenticated) } // Expect Unauthenticated
    }

    @Test
    fun `checkAuth should set Authenticated when user exists`() {
        every { firebaseAuth.currentUser } returns mockk<FirebaseUser>()  // mock Firebase user exists

        val observer = mockk<Observer<AuthState>>(relaxed = true)
        authViewModel.authState.observeForever(observer)

        authViewModel.checkAuth()

        verify { observer.onChanged(AuthState.Authenticated) } // Expect Authenticated
    }

    // THis is tests ensure that all items covered in Authentication test were tested.
    @Test
    fun `login with valid credentials sets Authenticated state`() {
        // Arrange
        val mockTask = mockk<Task<AuthResult>>(relaxed = true)
        every { mockTask.isSuccessful } returns true
        every { firebaseAuth.signInWithEmailAndPassword("test@example.com", "password123") } returns mockTask

        // Act
        authViewModel.login("test@example.com", "password123")

        // Assert
        verifySequence {
            authStateObserver.onChanged(AuthState.Loading)
            authStateObserver.onChanged(AuthState.Authenticated)
        }
    }

    @Test
    fun `login with invalid credentials sets Error state`() {
        // Arrange
        val mockTask = mockk<Task<AuthResult>>(relaxed = true)
        val exception = Exception("Invalid credentials")
        every { mockTask.isSuccessful } returns false
        every { mockTask.exception } returns exception
        every { firebaseAuth.signInWithEmailAndPassword("test@example.com", "password123") } returns mockTask

        // Act
        authViewModel.login("test@example.com", "password123")

        // Assert
        verifySequence {
            authStateObserver.onChanged(AuthState.Loading)
            authStateObserver.onChanged(AuthState.Error("Invalid credentials"))
        }
    }

    @Test
    fun `signup with valid data sets Authenticated state`() {
        // Arrange
        val mockTask = mockk<Task<AuthResult>>(relaxed = true)
        val mockUser = mockk<FirebaseUser>()
        every { mockTask.isSuccessful } returns true
        every { mockTask.result?.user } returns mockUser
        every { mockUser.uid } returns "user123"
        every { firebaseAuth.createUserWithEmailAndPassword("test@example.com", "password123") } returns mockTask
        every { firebaseAuth.currentUser } returns mockUser

        // Act
        authViewModel.signup("John Doe", "test@example.com", "password123", "SUV")

        // Assert
        verifySequence {
            authStateObserver.onChanged(AuthState.Loading)
            authStateObserver.onChanged(AuthState.Authenticated)
        }
    }

    @Test
    fun `signout sets Unauthenticated state`() {
        // Arrange
        every { firebaseAuth.signOut() } just Runs

        // Act
        authViewModel.signout()

        // Assert
        verify { authStateObserver.onChanged(AuthState.Unauthenticated) }
        verify { firebaseAuth.signOut() }
    }

    @Test
    fun `signup with empty email or password sets Error state`() {
        // Act
        authViewModel.signup("John Doe", "", "password", "SUV")

        // Assert
        verify { authStateObserver.onChanged(AuthState.Error("Email or Password cannot be empty")) }
    }

}

/*This tests:
* If IAuthViewModel methods(checkAuth, Login, Signup, Signout.) work with mock daat
* Logic function success/failure logic
* State changes under all scenarios
* */