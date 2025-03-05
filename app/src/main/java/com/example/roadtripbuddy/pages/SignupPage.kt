package com.example.roadtripbuddy.pages

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.roadtripbuddy.AuthState
import com.example.roadtripbuddy.AuthViewModel
import com.example.roadtripbuddy.R
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.example.roadtripbuddy.User
import com.tomtom.sdk.vehicle.Vehicle

//import com.example.playground.isPasswordString
//modifier: Modifier = Modifier, authViewModel:AuthViewModel

// a function to validate passwords for strength
fun isPasswordString(password:String) : Boolean
{
    val minLength = 8
    //checking if there is a UPPER case in the password
    // .any iterates through the string checking for the condition
    // it is referencing to it self
    // returns true if it matches
    val hasUpper = password.any {it.isUpperCase()}
    // now checking if it as a lower case
    val hasLower = password.any{ it.isLowerCase()}
    // checking if there is a digit in the string
    val hasDigit = password.any {it.isDigit()}
    val hasSpecialChar = password.any {"!@#$%".contains(it)}

    // validtae
    return password.length >= minLength && hasUpper && hasLower && hasDigit && hasSpecialChar
}
//@Preview
@Composable
fun SignupPage(navController: NavController, authViewModel:AuthViewModel) {
    // creating the instance of db
    val db = Firebase.firestore
// function to add the user to db when they auth
    fun addUser(name:String,password: String,vehicle: String)
    {
        // create User obj

        // add to db
    }




    // for the toast
    val context = LocalContext.current
    // var for the user name, using REMEMBER to preserve the state of the variable across compose functions
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var repass by remember { mutableStateOf("") }
    var isPasswordValid by remember { mutableStateOf(true) }
    var name by remember { mutableStateOf("") }
    var vehicle by remember { mutableStateOf("") }
// holds the auth state
    val authState = authViewModel.authState.observeAsState()

    LaunchedEffect(authState.value) {
        when(authState.value)
        {
            // this takes the user to the login page
            is AuthState.Authenticated -> navController.navigate("login")
            // any errors
            is AuthState.Error -> Toast.makeText(context,
                (authState.value as AuthState.Error).message, Toast.LENGTH_SHORT).show()
            else -> Unit
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.LightGray) // 🔹 Change background color here
            .padding(20.dp),

    )
    {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "Sign Up", modifier = Modifier.padding(20.dp))
            Row {
                Image(
                    painter = painterResource(id = R.drawable.name),
                    contentDescription = "Person Image for signup",
                    modifier = Modifier.weight(1f,fill=false)
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("name") }
                )
            }
            Row {
                Image(
                    painter = painterResource(id = R.drawable.car),
                    contentDescription = "Person Image for signup",
                    modifier = Modifier.weight(1f,fill=false)
                )
                OutlinedTextField(
                    value = vehicle,
                    onValueChange = { name = it },
                    label = { Text("Enter Vehicle Type (eg SUV, Sedan)") }
                )
            }
            Row (
            ){
                Image(
                    painter = painterResource(id = R.drawable.person),
                    contentDescription = "Person Image for signup",
                    modifier = Modifier.weight(1f,fill=false)
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") }
                )
            }
            Row {
                Image(
                    painter = painterResource(id = R.drawable.lock),
                    contentDescription = "Lock Image for signup",
                    modifier = Modifier.weight(1f,fill=true).size(65.dp)
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        isPasswordValid = isPasswordString(it)
                    },
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), // this adds the dots when typing the password
                    singleLine = true,
                    isError = !isPasswordValid // error handling
                    ,
                    colors = TextFieldDefaults.colors(),
//                    trailingIcon = {
//                        // error handling when the password is invalid
//                        if (!isPasswordValid) {
//                            Toast.makeText(context, "Password is Invalid!", Toast.LENGTH_SHORT)
//                                .show()
//                        }
//                        else{
//                            null
//                        }
//                    },
                )


            }
            // button
            Button(
                onClick =  {
                    if(!isPasswordValid)
                    {
                        Toast.makeText(context, "Password is Invalid!", Toast.LENGTH_SHORT)
                            .show()
                    }
                    else{
                        // singing up the user
                        authViewModel.signup(email,password)
                    }
                },
                modifier = Modifier.padding(16.dp),
                enabled = true,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    contentColor = Color.Black,
                    containerColor = Color.Gray
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 10.dp),
                border = BorderStroke(width = 2.dp, brush = SolidColor(Color.Black)),
                contentPadding = PaddingValues(
                    start = 20.dp,
                    top = 12.dp,
                    end = 20.dp,
                    bottom = 12.dp
                ),
                interactionSource = remember { MutableInteractionSource() }
            ) {
                Text(
                    text = "Submit",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    fontStyle = FontStyle.Italic,
                    fontFamily = FontFamily.Serif
                )
            }
            // if they already have an account
            TextButton(onClick = {
                //this will take them to the log in page
                navController.navigate("login")
            }) {
                Text(text = "Already have an account,Login!")
            }

        }
    }

    // adding

}