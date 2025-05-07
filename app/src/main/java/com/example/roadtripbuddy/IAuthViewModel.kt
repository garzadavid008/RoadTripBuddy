package com.example.roadtripbuddy

import androidx.lifecycle.LiveData

interface IAuthViewModel {
    val authState: LiveData<AuthState>
    fun login(email: String, password: String)
    fun signup(name: String, email: String, password: String, vehicle: String)
    fun signout()
    fun checkAuth()
}
/*This is an Interface that will mimic the behaviour of AuthViewModel
And will serve as a dependency for LoginPageTest, MockAuthViewModel
* Note 'signout' is considered a typo but no real errors so I won't bother.*/