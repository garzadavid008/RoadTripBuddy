package com.example.roadtripbuddy.mocks

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.roadtripbuddy.AuthState
import com.example.roadtripbuddy.IAuthViewModel
import com.example.roadtripbuddy.pages.isValidVehicleType


class MockAuthViewModel(
    private val loginSuccessful: Boolean = true,
    private val signupSuccessful: Boolean = true,
    private val loginError: String? = null,
    private val signupError: String? = null
) : IAuthViewModel {

    private val _authState = MutableLiveData<AuthState>()
    override val authState: LiveData<AuthState> = _authState

   // private val _isLoading = MutableLiveData(false)
   // override val isLoading: LiveData<Boolean> = _isLoading

    // Email format validation
    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
        //return email.contains("@") && email.contains(".") && email.isNotEmpty()
    }

    // Password validation (copied from SignupPage.kt)
    private fun isPasswordString(password: String): Boolean {
        val minLength = 8
        val hasUpper = password.any { it.isUpperCase() }
        val hasLower = password.any { it.isLowerCase() }
        val hasDigit = password.any { it.isDigit() }
        val hasSpecialChar = password.any { "!@#$%".contains(it) }
        return password.length >= minLength && hasUpper && hasLower && hasDigit && hasSpecialChar
    }

    // For mock login to test for success and failure, Now provide error messages
    override fun login(email: String, password: String) {
        _authState.postValue(AuthState.Loading) // Emit Loading state
        when {
            email.isEmpty() || password.isEmpty() -> {
                _authState.postValue(AuthState.Error("Email or Password cannot be empty"))
            }
            !isValidEmail(email) -> {
                _authState.postValue(AuthState.Error("Invalid email format"))
            }
            loginSuccessful -> {
                _authState.postValue(AuthState.Authenticated)
            }
            else -> {
                _authState.postValue(AuthState.Error(loginError ?: "Incorrect Username & Password"))
            }
        }
    }

        // For mock signup to test for success and failure, Now Provide error messages
        override fun signup(name: String, email: String, password: String, vehicle: String) {
            _authState.postValue(AuthState.Loading)
            when {
                name.isEmpty() || email.isEmpty() || password.isEmpty() || vehicle.isEmpty() -> {
                    _authState.postValue(AuthState.Error("All fields are required"))
                }
                !isValidEmail(email) -> {
                    _authState.postValue(AuthState.Error("Invalid email format"))
                }
                !isValidVehicleType(vehicle) -> {
                    _authState.postValue(AuthState.Error("Invalid vehicle type"))
                }
                !isPasswordString(password) -> {
                    _authState.postValue(AuthState.Error("Password is invalid"))
                }
                signupSuccessful -> {
                    _authState.postValue(AuthState.Authenticated)
                }
                else -> {
                    _authState.postValue(AuthState.Error(signupError ?: "Something went wrong. Try again"))
                }
            }
        }

        // For mock sign out
        override fun signout() {
            _authState.postValue(AuthState.Unauthenticated)
        }

        // For mock authorization
        override fun checkAuth() {
            _authState.postValue(
                if (loginSuccessful) AuthState.Authenticated else AuthState.Unauthenticated
            )
        }
    }
