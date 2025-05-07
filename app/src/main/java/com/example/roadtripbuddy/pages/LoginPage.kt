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
import com.example.roadtripbuddy.R
import com.example.roadtripbuddy.IAuthViewModel

@Composable
fun LoginPage(navController: NavController, authViewModel: IAuthViewModel) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var dialogErrorMessage by remember { mutableStateOf("") }
    var showResetDialog by remember { mutableStateOf(false) }
    var resetEmail by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }
    val authState by authViewModel.authState.observeAsState()

    LaunchedEffect(authState) {
        when (authState) {
            AuthState.Authenticated -> navController.navigate("map") {
                popUpTo("login") { inclusive = true }
            }
            is AuthState.Error -> {
                val message = (authState as AuthState.Error).message
                if (message.contains("reset", ignoreCase = true)) {
                    dialogErrorMessage = message
                } else {
                    errorMessage = message
                }
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
            is AuthState.Success -> {
                successMessage = (authState as AuthState.Success).message
                Toast.makeText(context, successMessage, Toast.LENGTH_SHORT).show()
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
            Text(text = "Login", modifier = Modifier.padding(20.dp))
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
                    onValueChange = { password = it; errorMessage = "" },
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(),
                    modifier = Modifier.semantics { testTag = "password_field" }
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
            TextButton(onClick = {
                navController.navigate("signup")
            }) {
                Text(text = "Don't have an account? Sign up here")
            }
            TextButton(onClick = { showResetDialog = true }) {
                Text(
                    text = "Forgot Password?",
                    modifier = Modifier.semantics { testTag = "forgot_password" }
                )
            }
            if (showResetDialog) {
                AlertDialog(
                    onDismissRequest = { showResetDialog = false; resetEmail = ""; successMessage = ""; dialogErrorMessage = "" },
                    title = { Text("Reset Password") },
                    text = {
                        Column {
                            OutlinedTextField(
                                value = resetEmail,
                                onValueChange = { resetEmail = it; dialogErrorMessage = "" },
                                label = { Text("Email") },
                                modifier = Modifier.semantics { testTag = "reset_email_field" }
                            )
                            if (successMessage.isNotEmpty()) {
                                Text(
                                    text = successMessage,
                                    color = Color.Green,
                                    modifier = Modifier.padding(8.dp).semantics { testTag = "success_message" }
                                )
                            }
                            if (dialogErrorMessage.isNotEmpty()) {
                                Text(
                                    text = dialogErrorMessage,
                                    color = Color.Red,
                                    modifier = Modifier.padding(8.dp).semantics { testTag = "error_message" }
                                )
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                authViewModel.resetPassword(resetEmail)
                            },
                            modifier = Modifier.semantics { testTag = "reset_submit_button" }
                        ) {
                            Text("Send")
                        }
                    },
                    dismissButton = {
                        Button(onClick = { showResetDialog = false; resetEmail = ""; successMessage = ""; dialogErrorMessage = "" }) {
                            Text("Cancel")
                        }
                    }
                )
            }
            TextButton(onClick = {
                navController.navigate("map")
            }) {
                Text(text = "Back to Map")
            }
        }
    }
}