package com.example.roadtripbuddy

//import jaandroid.util.Log // package does not exist (In my end anyways)
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import android.util.Log // this was added for logging

// added constructor to accept FirebaseAuth with a default value
// fixes to many arguments error in AuthViewModel(firebaseAuth) in AuthViewModelTest hopefully
class AuthViewModel(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel(), IAuthViewModel {

    // creating the fire base methods
    //private val auth : FirebaseAuth = FirebaseAuth.getInstance()// reposition
    // var to hold the authState
    // this is private and has a _ because this will be hidden from the UI for security
    private val _authState = MutableLiveData<AuthState>()
    // this will be used when speaking with the UI
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

    // function for the sign up
    override fun signup(name: String, email: String, password: String, vehicle: String) // changed to match the order of IAuthModel
    {
        //We must first check if the email or passwords strings are empty
        if(email.isEmpty() || password.isEmpty())
        {
            _authState.value = AuthState.Error("Email or Password cannot be empty")
            return// added to return that its empty for clarity
        }
        // set the state of the user to loading whenever they are in the login page
        _authState.value = AuthState.Loading
        auth.createUserWithEmailAndPassword(email,password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful)
                {
                    // the user was created !
                    _authState.value = AuthState.Authenticated
                    // creating the instance of db
                    val db = Firebase.firestore
                    // calling the user collection
                    val userCollection = db.collection("users")
                    // create User obj
                    val user = User(name,email,vehicle)
                    val fireBaseUser = auth.currentUser
                    val userId = fireBaseUser?.uid!!
                    // adding to the db with their specific auth iD
                    userCollection.document(userId).set(user.toMap()).addOnSuccessListener {
                        Log.d("Firestore", "User document added successfully!")
                    }
                        .addOnFailureListener { e ->
                            Log.w("Firestore", "Error adding user document", e)
                        }

                }else
                {
                    // firebase couldn't log them in. so provide the error
                    _authState.value = AuthState.Error(task.exception?.message?:"Something went wrong.Try again")
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
sealed class AuthState
{
    object  Authenticated : AuthState()
    object Unauthenticated: AuthState()
    object  Loading : AuthState() // when the user is loading .
    data class Error(val message:String) : AuthState()
}
// Sealed sub-objects can be converted to 'data object'
// changes done should ensure IAuthViewModel is implemented,
// current functions remain intact,
// Ensuring compatibility with:
// MainActivity.kt, LoginPage, SignupPage, and NavigationDrawer