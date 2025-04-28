package com.example.roadtripbuddy.TripSelect

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.unit.dp
import com.example.roadtripbuddy.data.Trip

@Composable
fun TripRow(
    trip: Trip,          // see note⇣
    onTripClick: () -> Unit,
    onTripDelete: () -> Unit,
    onTripRename: (String) -> Unit
) {
    var menuOpen by remember { mutableStateOf(false) }
    var editMode     by remember { mutableStateOf(false) }
    var editedName   by remember { mutableStateOf(trip.name) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onTripClick),
        tonalElevation = 2.dp,
        shape = MaterialTheme.shapes.medium,
        color = Color(0xFFdbf3fd)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (editMode) {
                OutlinedTextField(
                    value = editedName,
                    onValueChange = { editedName = it },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = {
                        editMode   = false
                        if (editedName.isNotBlank() && editedName != trip.name) {
                            onTripRename(editedName.trim())
                        } else {
                            editedName = trip.name     // reset if empty
                        }
                    }
                ) { Icon(Icons.Default.Check, null) }

                IconButton(
                    onClick = {
                        editMode   = false
                        editedName = trip.name
                    }
                ) { Icon(Icons.Default.Close, null) }
            } else {
                Text(
                    text  = trip.name,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                Text("${trip.waypointsList.size} stops", color = Color.Gray)

                IconButton(onClick = { menuOpen = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More")
                }
            }
        }
    }

    Box {
        DropdownMenu(
            expanded = menuOpen,
            onDismissRequest = { menuOpen = false }
        ) {
            DropdownMenuItem(
                text = { Text("Edit") },
                onClick = {
                    menuOpen = false
                    editMode = true
                })

            /* —— DELETE —— */
            DropdownMenuItem(
                text = { Text("Delete") },
                onClick = {
                    menuOpen = false
                    onTripDelete()
                })
        }
    }
}