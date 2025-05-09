package com.example.roadtripbuddy


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import android.util.Patterns
import android.util.Log // this was added for logging

// added constructor to accept FirebaseAuth with a default value

class AuthViewModel(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel(), IAuthViewModel {

    // creating the fire base methods
    // var to hold the authState
    private val _authState = MutableLiveData<AuthState>()
    override val authState: LiveData<AuthState> = _authState

    init {
        // everytime we launch an activity it will launch this
        checkAuth()
    }


    // a function to check if we are logged in in or not, Note Override modifier was added
    // to ensure AuthViewModel detects and implements IAuthViewModel otherwise IAuthViewModel members are "hidden".
    //Whatever that means.
    override fun checkAuth()
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

    override fun login(email:String,password:String)
    {
        //We must first check if the email or passwords strings are empty
        if(email.isEmpty() || password.isEmpty())
        {
            _authState.value = AuthState.Error("Email or Password cannot be empty")
            return// added to return that its empty for clarity
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
                    // firebase couldn't log them in. so provide the error
                    _authState.value = AuthState.Error(task.exception?.message?:"Something went wrong.Try again")
                }
            }
    }

    override fun resetPassword(email: String) {
        if (email.isEmpty()) {
            _authState.value = AuthState.Error("Email cannot be empty for reset")
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _authState.value = AuthState.Error("Invalid email format for reset")
            return
        }
        _authState.value = AuthState.Loading
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _authState.value = AuthState.Success("Password reset email sent")
                } else {
                    _authState.value = AuthState.Error(task.exception?.message ?: "Failed to send reset email")
                }
            }
    }

    // function for the sign up
    override fun signup(name: String, email: String, password: String) {
        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            _authState.value = AuthState.Error("All fields are required")
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _authState.value = AuthState.Error("Invalid email format")
            return
        }
        _authState.value = AuthState.Loading
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _authState.value = AuthState.Authenticated
                    val db = Firebase.firestore
                    val userCollection = db.collection("users")
                    val user = User(name, email)
                    val userId = auth.currentUser?.uid!!
                    userCollection.document(userId).set(user.toMap())
                        .addOnSuccessListener {
                            Log.d("Firestore", "User document added successfully!")
                        }
                        .addOnFailureListener { e ->
                            Log.w("Firestore", "Error adding user document", e)
                        }
                } else {
                    _authState.value = AuthState.Error(task.exception?.message ?: "Something went wrong. Try again")
                }
            }
    }

    // function to sign-out
    override fun signout() {
        // Sign out from Firebase and update state
        auth.signOut()
        _authState.value = AuthState.Unauthenticated
    }
}

// a class to handle the authenticated state of the user. if they are login or not
sealed class AuthState {
    object Authenticated : AuthState()
    object Unauthenticated : AuthState()
    object Loading : AuthState()
    data class Error(val message: String) : AuthState()
    data class Success(val message: String) : AuthState()
}
// Sealed sub-objects can be converted to 'data object'
// changes done should ensure IAuthViewModel is implemented,
// current functions remain intact,
// Ensuring compatibility with:
// MainActivity.kt, LoginPage, SignupPage, and NavigationDrawer