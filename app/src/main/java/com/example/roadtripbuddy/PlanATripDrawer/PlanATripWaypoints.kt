package com.example.roadtripbuddy.PlanATripDrawer

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.HapticFeedbackConstantsCompat
import androidx.core.view.ViewCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.roadtripbuddy.PlanATripViewModel
import com.example.roadtripbuddy.SearchManager
import com.example.roadtripbuddy.WaypointItem
import com.tomtom.sdk.search.model.result.SearchResult
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState
import java.time.ZoneId
import java.util.Date

@Composable
fun PlanATripWaypoints(
    viewModel: PlanATripViewModel = viewModel(),
    searchManager: SearchManager,
    onRoute: (PlanATripViewModel) -> Unit,
    resolveAndSuggest: (String, (List<Pair<String, Any?>>) -> Unit) -> Unit,
    onPlanWaypointEdit: (Pair<Int, WaypointItem>) -> Unit,
    onPlanWaypointAdd: () -> Unit
) {
    val eta by viewModel.ETA.collectAsState()
    val waypoints by viewModel.planWaypoints.collectAsState()
    val focusManager = LocalFocusManager.current

    // Using TextFieldValue to capture both text and selection
    val defaultLocation = searchManager.startLocationAddress


    val view = LocalView.current

    val lazyListState = rememberLazyListState()

    // Function to update the route and trigger onRoute when there is at least one waypoint and departAt is not null
    fun updateRoute() {
        Log.d("UPDATING ROUTE LIST", viewModel.planWaypoints.value.toString())
        if (waypoints.isNotEmpty())
            onRoute(viewModel)
    }

    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
        viewModel.moveWaypoint(from.index, to.index)

        ViewCompat.performHapticFeedback(
            view,
            HapticFeedbackConstantsCompat.SEGMENT_FREQUENT_TICK
        )
    }

    LaunchedEffect(Unit){
        // We grab the SearchResult of the defaultLocation AKA the location the user specified as the start
        if (viewModel.planWaypoints.value.isEmpty()){
            resolveAndSuggest(defaultLocation) { result -> // Calling resolveAndSuggest, which is an asynchronous call
                val searchResult = result.first().second as SearchResult
                viewModel.addPlanWaypoint(searchResult)// Initialize waypoints
            }
        }
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .size(width = 40.dp, height = 4.dp)
                    .background(color = Color.LightGray, shape = RoundedCornerShape(50))
                    .align(Alignment.CenterHorizontally)
            )
            //Spacer(modifier = Modifier.height(2.dp))
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically){
                Text("Plan A Trip", style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.weight(1f))
                // Plan Route button
                Button(
                    onClick = {
                        updateRoute()
                    },
                    modifier = Modifier.padding(vertical = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6ACFFF),
                        contentColor = Color.White
                    )
                ) {
                    Text("PlanRoute")
                }
            }
            // Composable function for input date and time, and returns a callback with either a null
            // or a valid LocalDateTime and then converts said LocalDateTime into a Date and stores it into the view models initialDeparture
            DepartureDateTimeInput(
                viewModel = viewModel,
                onValidTimeAndDate = { newDateTime ->
                    Log.d("Valid Time and Date", newDateTime.toString())
                    val departAt = Date.from(newDateTime.atZone(ZoneId.systemDefault()).toInstant())
                    viewModel.updateInitialDeparture(departAt)
                }
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth()){
                Text("Waypoints:")
                Spacer(modifier = Modifier.width(144.dp))
                Text("Hour:")
                Spacer(modifier = Modifier.width(32.dp))
                Text("Min:")
            }
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(modifier = Modifier.fillMaxWidth(), state = lazyListState) {
                itemsIndexed(waypoints, key = { _, waypoint -> waypoint.hashCode() }) { index, waypoint ->
                    ReorderableItem(reorderableLazyListState, key = waypoint.hashCode() ) { isDragging ->
                        val address by remember { mutableStateOf(waypoint.searchResult?.place?.address?.freeformAddress!!) }
                        var hour by remember { mutableStateOf(waypoint.hour.toString()) } // assuming waypoint.hour exists
                        var minute by remember { mutableStateOf(waypoint.minute.toString()) } // assuming waypoint.minute exists

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (index != 0){
                                PlanWaypointTextField(// Custom text field
                                    address = address,
                                    hour = hour,
                                    onHourChange = { newHour ->
                                        // If the text field is empty, make it zero. Else, initialize as normal
                                        hour = if (newHour == "") {
                                            "0"
                                        } else {
                                            newHour
                                        }
                                        viewModel.updateTimeSpent(
                                            index = index,
                                            newHour = hour.toInt(),
                                            newMinute = minute.toInt()
                                        )
                                    },
                                    minute = minute,
                                    onMinuteChange = { newMinute ->
                                        // If the text field is empty, make it zero. Else, initialize as normal
                                        minute = if (newMinute == "") {
                                            "0"
                                        } else {
                                            newMinute
                                        }
                                        viewModel.updateTimeSpent(
                                            index = index,
                                            newHour = hour.toInt(),
                                            newMinute = minute.toInt()
                                        )
                                    },
                                    onAddressClick = {
                                        onPlanWaypointEdit(Pair(index, waypoint))
                                    },
                                    onFocus = {},
                                    modifier = Modifier.draggableHandle(
                                        onDragStarted = {
                                            ViewCompat.performHapticFeedback(
                                                view,
                                                HapticFeedbackConstantsCompat.GESTURE_START
                                            )
                                        },
                                        onDragStopped = {
                                            ViewCompat.performHapticFeedback(
                                                view,
                                                HapticFeedbackConstantsCompat.GESTURE_END
                                            )
                                        },
                                    ),
                                )
                            } else {
                                PlanWaypointTextField(// Custom text field
                                    address = address!!,
                                    hour = "",
                                    onHourChange = {},
                                    minute = "",
                                    onMinuteChange = {},
                                    onAddressClick = {
                                        onPlanWaypointEdit(Pair(index, waypoint))
                                    },
                                    onFocus = {},
                                    modifier = Modifier.draggableHandle(
                                        onDragStarted = {
                                            ViewCompat.performHapticFeedback(
                                                view,
                                                HapticFeedbackConstantsCompat.GESTURE_START
                                            )
                                        },
                                        onDragStopped = {
                                            ViewCompat.performHapticFeedback(
                                                view,
                                                HapticFeedbackConstantsCompat.GESTURE_END
                                            )
                                        },
                                    ),
                                    ifFirstLocation = true
                                )
                            }

                            //REMOVE WAYPOINT BUTTON
                            if (waypoints.isNotEmpty()) {
                                IconButton(
                                    onClick = {
                                        viewModel.removePlanWaypoint(index)
                                    },
                                    modifier = Modifier.padding(start = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Remove Waypoint",
                                        tint = Color.Gray
                                    )
                                }
                            }

                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                // If waypointFlag is true and if waypoint size doesn't exceed 20
                if (waypoints.size < 20) { //Limiting the amount of waypoints user can add for now, change it for testing
                    item {
                        Button( //ADD WAYPOINT BUTTON
                            onClick = {
                                onPlanWaypointAdd()
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
                }
            }
        }
    }
}

