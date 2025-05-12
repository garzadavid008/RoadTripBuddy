package com.example.roadtripbuddy.TripSelect

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp

@Composable
fun NewTripDialog(
    onDone: (String) -> Unit,
    onDismiss: () -> Unit,
    nameError: Boolean
) {
    var name   by remember { mutableStateOf("") }

    // Simple full-screen scrim
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = .32f)),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape  = MaterialTheme.shapes.medium,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(24.dp)
        ) {
            Column(Modifier.padding(24.dp)) {
                if (nameError){
                    Text("Name is already used", color = MaterialTheme.colorScheme.error)
                }
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    isError = nameError,
                    label = { Text("Trip name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(16.dp))

                Row(Modifier.align(Alignment.End)) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = { onDone(name.trim()) },
                        enabled = name.isNotBlank()
                    ) {
                        Text("Done")
                    }
                }
            }
        }
    }
}