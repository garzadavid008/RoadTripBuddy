package com.example.roadtripbuddy.SearchDrawer

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.roadtripbuddy.SearchManager
import com.example.roadtripbuddy.SearchDrawerViewModel
import kotlinx.coroutines.delay

@Composable
fun RouteEditPage(
    viewModel: SearchDrawerViewModel = viewModel(),
    initialRouteSet: MutableState<Boolean>,
    initialDestination: String,
    searchManager: SearchManager,
    onBack: () -> Unit,
    onRoute: (SearchDrawerViewModel) -> Unit,
    onStartTrip: () -> Unit,
    performAutocomplete: (String, (List<String>) -> Unit) -> Unit
) {
    val eta by viewModel.ETA.collectAsState()
    val waypoints by viewModel.waypoints.collectAsState()
    val focusManager = LocalFocusManager.current
    var showDropdown by remember { mutableStateOf(false) }


    // This is a flag to not let user add more than one waypoint at a time
    var waypointFlag by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        if (viewModel.waypoints.value.isEmpty()) {
            viewModel.initializeWaypoints(initialDestination) //mInitialize waypoints
        }
    }

    // Calls onRoute once at the beginning
    LaunchedEffect(viewModel.waypoints) {
        if (viewModel.waypoints.value.isNotEmpty() && !initialRouteSet.value) {
            onRoute(viewModel) // Only calls when waypoints are available
            initialRouteSet.value = true
        }
    }
    // Function to update the route and trigger onRoute
    fun updateRoute() {
        Log.d("UPDATING ROUTE LIST", viewModel.waypoints.value.toString())
        onRoute(viewModel)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                })
            }
    ) {

        Text(
            text = eta,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Box(modifier = Modifier.clickable { onBack() }) {
                Text("\u2190 Back", color = Color.Blue)
            }
            Spacer(modifier = Modifier.height(16.dp))

            Text("Edit Route", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))

            Text("Start Location:")
            OutlinedTextField(
                value = searchManager.startLocationAddress, // Users location address from the searchManager
                onValueChange = {},
                label = { Text("Start") },
                enabled = false,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text("Waypoints:")
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                itemsIndexed(waypoints, key = { _, waypoint -> waypoint.hashCode() }) { index, waypoint ->
                    var query by remember { mutableStateOf(waypoint) }
                    var suggestions by rememberSaveable { mutableStateOf<List<String>>(emptyList()) }
                    var isFieldFocused by remember { mutableStateOf(false) }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = query,
                            onValueChange = { newValue ->
                                query = newValue
                            },
                            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(
                                onDone = { //When user presses done ->
                                    viewModel.updateWaypoint(index, query)
                                    updateRoute()
                                    focusManager.clearFocus()
                                }
                            ),
                            label = { Text("Waypoint ${index + 1}") },
                            modifier = Modifier
                                .weight(1f)
                                .onFocusChanged { focusState ->
                                    isFieldFocused = focusState.isFocused
                                    if (!focusState.isFocused) {
                                        suggestions = emptyList()
                                    }
                                }
                                .onKeyEvent { keyEvent -> // For testing basically, makes the Enter button on our keyboards commit a waypoint[index] change
                                    if (keyEvent.type == KeyEventType.KeyUp && keyEvent.key == Key.Enter) {
                                        viewModel.updateWaypoint(index, query)
                                        updateRoute()
                                        waypointFlag = true
                                        focusManager.clearFocus()
                                        true
                                    } else {
                                        false
                                    }
                                }
                        )
                        //REMOVE WAYPOINT BUTTON
                        if (waypoints.size > 1) { // Waypoint size should always be > 1
                            IconButton(
                                onClick = {
                                    viewModel.removeWaypoint(index)
                                    Handler(Looper.getMainLooper()).postDelayed({
                                        updateRoute()
                                    }, 100)
                                    // If the flag is false make it true, if we don't do this the user gets stuck
                                    if (!waypointFlag) {
                                        waypointFlag = true
                                    }
                                },
                                modifier = Modifier.padding(start = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Remove Waypoint",
                                    tint = Color.Red
                                )
                            }
                        }

                    }

                    LaunchedEffect(query) { //Pulsing the API call for autocomplete
                        if (query.isNotEmpty()) {
                            delay(300)
                            performAutocomplete(query) { initSuggestions ->
                                suggestions = initSuggestions.distinct()
                            }
                        } else {
                            suggestions = emptyList()
                        }
                    }

                    // Only display autocomplete results/suggestions when the user is actively using the textField and suggestions are not empty
                    if (isFieldFocused && suggestions.isNotEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White)
                        ) {
                            suggestions.forEach { suggestion ->
                                Text(
                                    text = suggestion,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            query = suggestion
                                            viewModel.updateWaypoint(index, suggestion)
                                            Handler(Looper.getMainLooper()).postDelayed({
                                                updateRoute()
                                            }, 100)
                                            suggestions = emptyList()
                                            waypointFlag = true
                                            focusManager.clearFocus()
                                        }
                                        .padding(8.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }
                // If waypointFlag is true and if waypoint size doesnt exceed 20
                if (waypointFlag && waypoints.size < 20) { //Limiting the amount of waypoints user can add for now, change it for testing
                    item {
                        Button( //ADD WAYPOINT BUTTON
                            onClick = {
                                viewModel.addWaypoint()
                                //on click make the flag false to not let the user add another waypoint until this one is initialized
                                waypointFlag = false
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF6ACFFF),
                                contentColor = Color.White
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Waypoint",
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text("Add Waypoint")
                        }
                    }
                    item {
                        Button(
                            onClick = { onStartTrip() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = Color.White
                            )
                        ) {
                            Text("Start Directions")
                        }
                    }
                }
            }
        }
    }
}