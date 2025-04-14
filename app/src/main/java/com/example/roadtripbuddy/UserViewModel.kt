package com.example.roadtripbuddy

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
//import com.example.roadtripbuddy.User


// user view model to view user data
class UserViewModel : ViewModel(){
    var user by mutableStateOf<User?>(null)
       // private set // setting the user var above private so only methods in the class can modify it


    init // this runs when the class is created
    {
     fetchUser()
    }

    private fun fetchUser()
    {
        viewModelScope.launch {
            user = getUserFromFirestore()
        }
    }

    // making async function to fetch firestore data
suspend fun getUserFromFirestore(): User? {
    // grab the user id
    val userID = FirebaseAuth.getInstance().currentUser?.uid ?: return null // if we cannot grab the user id, return null
    val db = Firebase.firestore
    // declaring the collection
    val userCollection = db.collection("users")

    return try
    {
        // grabbing the user document
        val document = userCollection.document(userID).get().await()
        document.toObject(User::class.java) ?: return null // returning null if the document does not exist
    } catch (e:Exception)
    {
        Log.w("Firestore","Error",e)
        null
    }

}



}