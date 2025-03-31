package com.example.roadtripbuddy.PlanATripDrawer

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.roadtripbuddy.SearchManager
import com.example.roadtripbuddy.PlanATripViewModel
import kotlinx.coroutines.delay
import java.time.ZoneId
import java.util.Date

@Composable
fun PlanATripWaypoints(
    viewModel: PlanATripViewModel = viewModel(),
    searchManager: SearchManager,
    onRoute: (PlanATripViewModel, Date) -> Unit,
    performAutocomplete: (String, (List<String>) -> Unit) -> Unit
) {
    val eta by viewModel.ETA.collectAsState()
    val waypoints by viewModel.planWaypoints.collectAsState()
    val focusManager = LocalFocusManager.current
    // Using TextFieldValue to capture both text and selection
    val defaultLocation = searchManager.startLocationAddress
    var startLocationQuery by remember {
        mutableStateOf(TextFieldValue(defaultLocation, selection = TextRange(defaultLocation.length)))
    }
    // This is a flag to not let user add more than one waypoint at a time
    var waypointFlag by remember { mutableStateOf(true) }
    // This is a Date data type variable that holds when the users departure time and date
    var departAt: Date? by remember { mutableStateOf(null) }

    // Function to update the route and trigger onRoute when there is at least one waypoint and departAt is not null
    fun updateRoute() {
        Log.d("UPDATING ROUTE LIST", viewModel.planWaypoints.value.toString())
        if (waypoints.isNotEmpty() && departAt != null)
            onRoute(viewModel, departAt!!)
    }
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { focusManager.clearFocus() }
        )
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
            Spacer(modifier = Modifier.height(16.dp))

            Text("Plan A Trip", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(8.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Start Location and Departure Time and Date:")
                OutlinedTextField(
                    value = startLocationQuery, // User's location address from the searchManager
                    onValueChange = {startLocationQuery = it},
                    label = { Text("Start") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(modifier = Modifier.height(4.dp))

            // Composable function for input date and time, and returns a callback with either a null
            // or a valid LocalDateTime and then converts said LocalDateTime into a Date and stores it into departAt
            DepartureDateTimeInput(
                onValidTimeAndDate = { newDateTime ->
                    Log.d("Valid Time and Date", newDateTime.toString())
                    departAt = Date.from(newDateTime.atZone(ZoneId.systemDefault()).toInstant())
                }
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth()){
                Text("Waypoints:")
                Spacer(modifier = Modifier.width(156.dp))
                Text("Time Spent:")
            }
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                itemsIndexed(waypoints, key = { _, waypoint -> waypoint.hashCode() }) { index, waypoint ->
                    var query by remember { mutableStateOf(waypoint.address) }
                    var timeSpent by remember { mutableStateOf("") }
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
                                    viewModel.updatePlanWaypoints(
                                        index = index,
                                        newAddress = query,
                                        newTime = timeSpent.toInt()
                                    )
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
                                        viewModel.updatePlanWaypoints(
                                            index = index,
                                            newAddress = query,
                                            newTime = timeSpent.toInt()
                                        )
                                        updateRoute()
                                        focusManager.clearFocus()
                                        true
                                    } else {
                                        false
                                    }
                                }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        // The "Time Spent" box
                        OutlinedTextField(
                            value = timeSpent,
                            onValueChange = { newValue ->
                                timeSpent = newValue.filter { it.isDigit()}
                            },
                            label = { Text("Min") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), // number keyboard shows
                            modifier = Modifier.width(100.dp) // Adjust width as desired
                        )
                        //REMOVE WAYPOINT BUTTON
                        if (waypoints.isNotEmpty()) {
                            IconButton(
                                onClick = {
                                    viewModel.removePlanWaypoint(index)
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
                                            viewModel.updatePlanWaypoints(
                                                index = index,
                                                newAddress = suggestion,
                                                newTime = timeSpent.toInt()
                                            )
                                            Handler(Looper.getMainLooper()).postDelayed({
                                                updateRoute()
                                            }, 100)
                                            suggestions = emptyList()
                                            focusManager.clearFocus()
                                        }
                                        .padding(8.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (waypointFlag || waypoints.size < 20) { //Limiting the amount of waypoints user can add for now, change it for testing
                    item {
                        Button( //ADD WAYPOINT BUTTON
                            onClick = {
                                viewModel.addPlanWaypoint()
                                waypointFlag = false
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
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
                }
            }
        }
    }
}

