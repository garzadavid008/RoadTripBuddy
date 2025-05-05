package com.example.roadtripbuddy

import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.auth.UserRecord
import com.google.firebase.auth.FirebaseAuthException

class SearchForUser {

 @Composable
fun userLookUP(navController: NavController, authViewModel:AuthViewModel){
   var text by remember { mutableStateOf("") }

      OutlinedTextField(
          value = text,
          onValueChange = { text = it },
          label = { Text("Label") }
        )
    }
}