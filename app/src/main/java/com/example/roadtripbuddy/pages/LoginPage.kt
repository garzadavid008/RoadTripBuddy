package com.example.roadtripbuddy.pages

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.* // Contains Arrangement, Box, Column, PaddingValues, Row, fillMaxSize, padding, size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.* // Contains Button, ButtonDefaults, OutlinedTextField, Text, TextButton, TextFieldDefaults (should contain keyboard options)
import androidx.compose.runtime.* // Contains Composable, Launched Effect, getValue, LiveData.observeAsState, MutableStateof, remember, setvalue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
//import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.roadtripbuddy.AuthState
//import com.example.roadtripbuddy.AuthViewModel
import com.example.roadtripbuddy.R
import com.example.roadtripbuddy.IAuthViewModel
//import androidx.lifecycle.compose.collectAsStateWithLifecycle

//  @Preview
    @Composable
    fun LoginPage(navController: NavController, authViewModel: IAuthViewModel) {

    // for the toast
    val context = LocalContext.current
    // var for the user name, using REMEMBER to preserve the state of the variable across compose functions
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    //var repass by remember { mutableStateOf("") }
    val authState by authViewModel.authState.observeAsState()

    LaunchedEffect(authState) {
        // Handles all possible AuthState cases, including null initial state
        when (authState) {
            AuthState.Authenticated -> navController.navigate("map") {
                popUpTo("login") { inclusive = true }
            }
            is AuthState.Error -> {
                errorMessage = (authState as AuthState.Error).message
                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            }
            AuthState.Loading, AuthState.Unauthenticated, null -> Unit
        }
    }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.LightGray) // 🔹 Change background color here
                .padding(20.dp),

            ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = "Login", modifier = Modifier.padding(20.dp))
                Row {// removed the empty '()' that was tied to lambda /now more clean
                    Image(
                        painter = painterResource(id = R.drawable.person),
                        contentDescription = "Person Image for signup",
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it; errorMessage = null }, // 4/26 reset errorMessage to null when user starts typing in email or password to clear errors
                        label = { Text("Email") },
                        modifier = Modifier.semantics { testTag = "email_field" }
                    )
                }
                // Additions added at 4/19/25
                Row {
                    Image(
                        painter = painterResource(id = R.drawable.lock),
                        contentDescription = "Lock Image for signup",
                        modifier = Modifier.weight(1f, fill = true).size(65.dp)
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it; errorMessage = null }, // or password to clear errors
                        label = { Text("Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), // this adds the dots when typing the password
                        singleLine = true,
                        colors = TextFieldDefaults.colors(),
                        modifier = Modifier.semantics { testTag = "password_field" }
                    )
                }
                //Error Message addition 4/19/25
                errorMessage?.let {
                    Text(
                        text = it,
                        color = Color.Red,
                        modifier = Modifier.padding(8.dp).semantics { testTag = "error_message" },
                        maxLines = 1
                    )
                }
                // button
                Button(
                    onClick = {
                        when { // added 4/25 ensure an error message is sent for empty inputs
                            email.isEmpty() || password.isEmpty() -> {
                                errorMessage = "Email or Password cannot be empty"
                                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                            }
                            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                                errorMessage = "Invalid email format"
                                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                            }
                            else -> {
                                authViewModel.login(email, password)
                            }
                        }
                    },
                    modifier = Modifier
                        .padding(16.dp)
                        .semantics { testTag = "submit_button" },
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
                // if they don't have an account
                TextButton(onClick = {
                    //this will take you to sign up page
                    navController.navigate("signup")
                }) {
                    Text(text = "Don't have an account? Sign up here")
                }
                // take them back to home
                TextButton(onClick = {
                    //this will take you back to the map page
                    navController.navigate("map")
                }) {
                    Text(text = "Back to Map")
                }
            }
        }
    }
