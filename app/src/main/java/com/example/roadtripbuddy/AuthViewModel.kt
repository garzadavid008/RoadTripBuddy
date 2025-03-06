package com.example.roadtripbuddy

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

class AuthViewModel: ViewModel()  {

    // creating the fire base methods

    private val auth : FirebaseAuth = FirebaseAuth.getInstance()

    // var to hold the authState
    // this is private and has a _ because this will be hidden from the UI for security
    private val _authState = MutableLiveData<AuthState>()
    // this will be used when speaking with the UI
    val authState: LiveData<AuthState> = _authState

    init {
        // everytime we launch an activity it will launch this
        checkAuth()
    }


    // a function to check if we are loggin in or not
    fun checkAuth()
    {
        // USER IS NOT LOGGED IN
        if(auth.currentUser == null)
        {
            _authState.value = AuthState.Unauthenticated
        }
        else
        {
            _authState.value = AuthState.Authenticated
        }
    }


    fun login(email:String,password:String)
    {
        //We must first check if the email or passwords strings are empty
        if(email.isEmpty() || password.isEmpty())
        {
            _authState.value = AuthState.Error("Email or Password cannot be empty")
        }
        // set the state of the user to loading whenever they are in the login page
        _authState.value = AuthState.Loading
        auth.signInWithEmailAndPassword(email,password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful)
                {
                    // the user is logged in !
                    _authState.value = AuthState.Authenticated
                }else
                {
                    // firebase couldnt log them in. so provide the error
                    _authState.value = AuthState.Error(task.exception?.message?:"Something went wrong.Try again")
                }
            }
    }

    // function for the sign up
    fun signup(email:String,password:String)
    {
        //We must first check if the email or passwords strings are empty
        if(email.isEmpty() || password.isEmpty())
        {
            _authState.value = AuthState.Error("Email or Password cannot be empty")
        }
        // set the state of the user to loading whenever they are in the login page
        _authState.value = AuthState.Loading
        auth.createUserWithEmailAndPassword(email,password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful)
                {
                    // the user was created !
                    _authState.value = AuthState.Authenticated
                }else
                {
                    // firebase couldnt log them in. so provide the error
                    _authState.value = AuthState.Error(task.exception?.message?:"Something went wrong.Try again")
                }
            }
    }

    // function to signout
    fun signout()
    {
        // change the auth to Unauth
        _authState.value = AuthState.Unauthenticated;
    }

}

// a class to handle the authenticated state of the user. if they are login or not
sealed class AuthState
{
    object  Authenticated : AuthState()
    object Unauthenticated: AuthState()
    object  Loading : AuthState() // when the user is loading .
    data class Error(val message:String) : AuthState()
}