package com.example.roadtripbuddy.pages

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.roadtripbuddy.AuthState
import com.example.roadtripbuddy.IAuthViewModel
import com.example.roadtripbuddy.R

fun isPasswordString(password: String): Boolean {
    val minLength = 8
    val hasUpper = password.any { it.isUpperCase() }
    val hasLower = password.any { it.isLowerCase() }
    val hasDigit = password.any { it.isDigit() }
    val hasSpecialChar = password.any { "!@#$%".contains(it) }
    return password.length >= minLength && hasUpper && hasLower && hasDigit && hasSpecialChar
}

fun isValidVehicleType(vehicle: String): Boolean {
    val validTypes = listOf("SUV", "Sedan", "Truck", "Van", "Hatchback")
    return vehicle.trim().isNotEmpty() && validTypes.any { it.equals(vehicle.trim(), ignoreCase = true) }
}

@Composable
fun SignupPage(navController: NavController, authViewModel: IAuthViewModel) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isPasswordValid by remember { mutableStateOf(true) }
    var name by remember { mutableStateOf("") }
    var vehicle by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    val authState by authViewModel.authState.observeAsState()

    LaunchedEffect(authState) {
        when (authState) {
            AuthState.Authenticated -> navController.navigate("map") {
                popUpTo("signup") { inclusive = true }
            }
            is AuthState.Error -> {
                errorMessage = (authState as AuthState.Error).message
                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            }
            is AuthState.Success -> {
                Toast.makeText(context, (authState as AuthState.Success).message, Toast.LENGTH_SHORT).show()
            }
            AuthState.Loading, AuthState.Unauthenticated, null -> Unit
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.LightGray)
            .padding(20.dp),
    ) {
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
                    modifier = Modifier.weight(1f, fill = false)
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; errorMessage = "" },
                    label = { Text("name") },
                    modifier = Modifier.semantics { testTag = "name_field" }
                )
            }
            Row {
                Image(
                    painter = painterResource(id = R.drawable.ic_car),
                    contentDescription = "Person Image for signup",
                    modifier = Modifier.weight(1f, fill = false)
                )
                OutlinedTextField(
                    value = vehicle,
                    onValueChange = { vehicle = it; errorMessage = "" },
                    label = { Text("Enter Vehicle Type (eg SUV, Sedan)") },
                    modifier = Modifier.semantics { testTag = "vehicle_field" }
                )
            }
            Row {
                Image(
                    painter = painterResource(id = R.drawable.person),
                    contentDescription = "Person Image for signup",
                    modifier = Modifier.weight(1f, fill = false)
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it; errorMessage = "" },
                    label = { Text("Email") },
                    modifier = Modifier.semantics { testTag = "email_field" }
                )
            }
            Row {
                Image(
                    painter = painterResource(id = R.drawable.lock),
                    contentDescription = "Lock Image for signup",
                    modifier = Modifier.weight(1f, fill = true).size(65.dp)
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        isPasswordValid = isPasswordString(it)
                        errorMessage = ""
                    },
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    isError = !isPasswordValid,
                    colors = TextFieldDefaults.colors(),
                    modifier = Modifier.semantics { testTag = "password_field" }
                )
            }
            Row {
                Image(
                    painter = painterResource(id = R.drawable.lock),
                    contentDescription = "Lock Image for confirm password",
                    modifier = Modifier.weight(1f, fill = true).size(65.dp)
                )
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it; errorMessage = "" },
                    label = { Text("Confirm Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    isError = confirmPassword.isNotEmpty() && confirmPassword != password,
                    colors = TextFieldDefaults.colors(),
                    modifier = Modifier.semantics { testTag = "confirm_password_field" }
                )
            }
            if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    color = Color.Red,
                    modifier = Modifier.padding(8.dp).semantics { testTag = "error_message" },
                    maxLines = 1
                )
            }
            Button(
                onClick = {
                    when {
                        name.isEmpty() || vehicle.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() -> {
                            errorMessage = "All fields are required"
                            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                        }
                        !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                            errorMessage = "Invalid email format"
                            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                        }
                        !isValidVehicleType(vehicle) -> {
                            errorMessage = "Invalid vehicle type"
                            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                        }
                        !isPasswordValid -> {
                            errorMessage = "Password is invalid"
                            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                        }
                        password != confirmPassword -> {
                            errorMessage = "Passwords do not match"
                            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                        }
                        else -> {
                            authViewModel.signup(name, email, password, vehicle)
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
            TextButton(onClick = {
                navController.navigate("login")
            }) {
                Text(text = "Already have an account, Login!")
            }
            TextButton(onClick = {
                navController.navigate("map")
            }) {
                Text(text = "Back to Map")
            }
        }
    }
}