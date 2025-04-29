package com.example.roadtripbuddy.pages

//import android.provider.ContactsContract.CommonDataKinds.Email
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.* // Contains Arrangement, Box, Column, PaddingValues, Row, fillMaxSize, padding, size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.* // Contains Button, ButtonDefaults, OutlinedTextField, Text, TextButton, TextFieldDefaults should contain keyboard options
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
//import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.roadtripbuddy.AuthState
import com.example.roadtripbuddy.IAuthViewModel
//import com.example.roadtripbuddy.AuthViewModel
import com.example.roadtripbuddy.R

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
    // validate
    return password.length >= minLength && hasUpper && hasLower && hasDigit && hasSpecialChar
}

// added for tests for vehicle valid,invalid,empty, ect
fun isValidVehicleType(vehicle: String): Boolean {
    val validTypes = listOf("SUV", "Sedan", "Truck", "Van", "Hatchback")
    return vehicle.trim().isNotEmpty() && validTypes.any { it.equals(vehicle.trim(), ignoreCase = true) }
}

//@Preview
@Composable
fun SignupPage(navController: NavController, authViewModel: IAuthViewModel) {

    // for the toast
    val context = LocalContext.current
    // var for the user name, using REMEMBER to preserve the state of the variable across compose functions
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    //var repass by remember { mutableStateOf("") }
    var isPasswordValid by remember { mutableStateOf(true) }
    var name by remember { mutableStateOf("") }
    var vehicle by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    // holds the auth state
    val authState by authViewModel.authState.observeAsState()

    LaunchedEffect(authState) {
        // Handles all possible AuthState cases, including null initial state
        when (authState) {
            AuthState.Authenticated -> navController.navigate("map") {
                popUpTo("signup") { inclusive = true }
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
                    label = { Text("name") },
                    modifier = Modifier.semantics { testTag = "name_field" }
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
                    onValueChange = { vehicle = it },
                    label = { Text("Enter Vehicle Type (eg SUV, Sedan)") },
                    modifier = Modifier.semantics { testTag = "vehicle_field" }
                )
            }
            Row { // removed the empty '()' that was tied to lambda /now more clean
                Image(
                    painter = painterResource(id = R.drawable.person),
                    contentDescription = "Person Image for signup",
                    modifier = Modifier.weight(1f, fill = false)
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.semantics { testTag = "email_field" }
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
                    isError = !isPasswordValid, // error handling
                    colors = TextFieldDefaults.colors(),
                    modifier = Modifier.semantics { testTag = "password_field" }
                )
            }
            // Additions added at 4/19/25
            Row {
                Image(
                    painter = painterResource(id = R.drawable.lock),
                    contentDescription = "Lock Image for confirm password",
                    modifier = Modifier.weight(1f, fill = true).size(65.dp)
                )
                // Added 4/19/2025
                // Add to test password confirmations and to check if confirming password matches confirmed password
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    isError = confirmPassword.isNotEmpty() && confirmPassword != password,
                    colors = TextFieldDefaults.colors(),
                    modifier = Modifier.semantics { testTag = "confirm_password_field" }
                )
            }
            //Error Message addition 4/19/25
            errorMessage?.let {
                Text(
                    text = it,
                    color = Color.Red,
                    modifier = Modifier.padding(8.dp).semantics { testTag = "error_message" } // .testtag added 4/21/25
                )
            }
            // button
            Button(
                onClick =  {
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
                            authViewModel.signup(name, email, password, vehicle) // Fixed order to match Signuptests
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
            // if they already have an account
            TextButton(onClick = {
                // this will take them to the login page
                navController.navigate("login")
            }) {
                Text(text = "Already have an account, Login!")
            }
            // take them back to home
            TextButton(onClick = {
                //this will take them back to the map page
                navController.navigate("map")
            }) {
                Text(text = "Back to Map")
            }

        }
    }

    // If need to add anything more do it here

}