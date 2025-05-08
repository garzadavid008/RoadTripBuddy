package com.example.roadtripbuddy.SearchDrawer

import android.util.Log
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
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
import com.example.roadtripbuddy.SearchDrawerViewModel
import com.example.roadtripbuddy.SearchManager
import com.tomtom.sdk.search.model.SearchResultType
import com.tomtom.sdk.search.model.result.SearchResult
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
fun RouteEditPage(
    viewModel: SearchDrawerViewModel = viewModel(),
    routeFlag: MutableState<Boolean>,
    searchManager: SearchManager,
    onBack: () -> Unit,
    onRoute: (SearchDrawerViewModel) -> Unit,
    onStartTrip: () -> Unit,
    onWaypointEdit: (Pair<Int, SearchResult>) -> Unit,
    onWaypointAdd: () -> Unit
) {
    val eta by viewModel.ETA.collectAsState()
    val waypoints by viewModel.waypoints.collectAsState()
    val focusManager = LocalFocusManager.current
    val view = LocalView.current

    val lazyListState = rememberLazyListState()

    // calls route on composition only if the route hasnt been updated via routeFlag
    LaunchedEffect(Unit) {
        if (viewModel.waypoints.value.isNotEmpty() && !routeFlag.value) {
            Log.d("UPDATING ROUTE LIST", viewModel.waypoints.value.toString())
            onRoute(viewModel) // Only calls when waypoints are available
            routeFlag.value = true
        }
    }

    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
        viewModel.moveWaypoint(from.index, to.index)

        ViewCompat.performHapticFeedback(
            view,
            HapticFeedbackConstantsCompat.SEGMENT_FREQUENT_TICK
        )

        onRoute(viewModel)
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
            Box(modifier = Modifier.clickable { onBack() }) {
                Text("\u2190 Back", color = Color.Blue)
            }
            Spacer(modifier = Modifier.height(16.dp))

            Text("Edit Route", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))

            Text("Waypoints:")
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(modifier = Modifier.fillMaxWidth(), state = lazyListState) {
                itemsIndexed(waypoints, key = { _, waypoint -> waypoint.hashCode() }) { index, waypoint ->
                    ReorderableItem(reorderableLazyListState, key = waypoint.hashCode() ) { isDragging ->
                        var address by remember { mutableStateOf(waypoint?.place?.address?.freeformAddress!!) }

                        LaunchedEffect(waypoint) {
                            if (waypoint?.type == SearchResultType.Poi) {
                                searchManager.toPoi(waypoint.searchResultId) { poiResult ->
                                    // safely grab first POI name
                                    val poiName = poiResult
                                        ?.poiDetails
                                        ?.poi
                                        ?.names
                                        ?.firstOrNull()
                                        .orEmpty()

                                    // 3) update your state – Compose will recompose and show the new name
                                    if (poiName.isNotBlank()) {
                                        address = poiName
                                    }
                                }
                            }
                        }

                        val label = if (index < waypoints.lastIndex) {
                            // take the code‐point of 'A', add index, then turn back into a Char
                            ( 'A'.code + index ).toChar().toString()
                        } else {
                            "🏁"
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(end = 8.dp)
                            )

                            WaypointTextField(
                                address = address,
                                onClick = {
                                    onWaypointEdit(Pair(index, waypoint!!))
                                          },
                                modifier = Modifier
                                    .weight(1f)
                                    .draggableHandle(
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
                                )

                            )
                            //REMOVE WAYPOINT BUTTON
                            // Makes sure the user can remove waypoints while in the middle of dragging
                            if (!isDragging && waypoints.size > 1) { // Waypoint size should always be > 1
                                IconButton(
                                    onClick = {
                                        viewModel.removeWaypoint(index)
                                        onRoute(viewModel)
                                    },
                                    modifier = Modifier.padding(start = 8.dp)
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

                if (waypoints.size < 20) { //Limiting the amount of waypoints user can add for now, change it for testing
                    item {
                        Button( //ADD WAYPOINT BUTTON
                            onClick = { onWaypointAdd() },
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