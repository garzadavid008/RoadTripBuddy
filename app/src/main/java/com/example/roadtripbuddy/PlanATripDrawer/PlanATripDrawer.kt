package com.example.roadtripbuddy.PlanATripDrawer

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.composables.core.BottomSheet
import com.composables.core.SheetDetent
import com.composables.core.SheetDetent.Companion.FullyExpanded
import com.composables.core.rememberBottomSheetState
import com.example.roadtripbuddy.Autocomplete
import com.example.roadtripbuddy.PlacesViewModel
import com.example.roadtripbuddy.PlanATripViewModel
import com.example.roadtripbuddy.PlanMap
import com.example.roadtripbuddy.RouteLegInfo
import com.example.roadtripbuddy.SearchDrawer.LocationDetails
import com.example.roadtripbuddy.SearchDrawer.LocationDetailsPage
import com.example.roadtripbuddy.WaypointItem
import com.example.roadtripbuddy.pages.PlaceListPage
import com.tomtom.sdk.search.model.result.SearchResult

//Compose for the Search/Route page
@SuppressLint("StateFlowValueCalledInComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanATripDrawer(
    onMapFocus: MutableState<Boolean>,
    viewModel: PlanATripViewModel = viewModel(),
    placesViewModel: PlacesViewModel,
    planMap: PlanMap,
    onBack: (Boolean) -> Unit
) {
    Log.d("Debug", "PlanDrawer onMapFocus: $onMapFocus")

    val peek = SheetDetent(identifier = "peek") { containerHeight, sheetHeight ->
        containerHeight * 0.1f
    }

    val half = SheetDetent(identifier = "half") { containerHeight, sheetHeight ->
        containerHeight * 0.5f
    }

    val sheetState = rememberBottomSheetState(
        initialDetent = half,
        detents = listOf(peek, half, FullyExpanded)
    )

    var ifAutocomplete by remember { mutableStateOf(false) }
    var routeLegInfo by remember { mutableStateOf(false) }
    val routeFlag  = rememberSaveable { mutableStateOf(false) }
    var planWaypointPair by remember { mutableStateOf<Pair<Int, WaypointItem?>?>(null) } // For the autocomplete composable page,
    val selectedRoutePair by viewModel.selectedRoutePair
    var showWaypointSuggestions by remember { mutableStateOf(false) }
    var saveDialog by remember { mutableStateOf(false) }
    var locationDetails by remember { mutableStateOf(false) }

    val focusManager =  LocalFocusManager.current
    var isFocused by remember { mutableStateOf(false) }

    val places by placesViewModel.restaurants.collectAsState()
    val waypoints by viewModel.planWaypoints.collectAsState()
    val initialDeparture by viewModel.initialDeparture.collectAsState()
    val selectedWaypoint by viewModel.selectedWaypoint

    //Snapshot of the waypoint list
    val initialWaypoints = remember { waypoints.toList() }
    //Snapshot of the initialDeparture (time)
    val initialDepartureSnapshot = remember { initialDeparture}

    if (sheetState.currentDetent != peek){
        onMapFocus.value = false
    }

    LaunchedEffect(places) {
        Log.d("Chris", "places updated: ${places.size}")
        if (places.isNotEmpty() && places.size>1) {
            Log.d("Chris", "places updatedAA: ${places.size}")
            showWaypointSuggestions = true
            sheetState.jumpTo(half)
        }
    }

    // If selectedRoutePair sees changes
    LaunchedEffect(selectedRoutePair) {
        if(selectedRoutePair != null){
            // show route info page if a route has been clicked on
            routeLegInfo = true
        }
        // if we detect a change in the selectedRoutePair and its null, click off the routeLegInfo page
        else {
            routeLegInfo = false
        }
    }

    LaunchedEffect(selectedWaypoint) {
        if (selectedWaypoint != null){
            locationDetails = true
            sheetState.jumpTo(half)
        }
    }

    LaunchedEffect(isFocused) {
        Log.d("isFocused", isFocused.toString())
        if (isFocused){
            sheetState.jumpTo(FullyExpanded)
        }
    }

    LaunchedEffect(sheetState.currentDetent) {
        if(sheetState.currentDetent != FullyExpanded){
            focusManager.clearFocus()
            isFocused = false
        }
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        IconButton(
            onClick = {
                saveDialog = true
            },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color(0xFF2ebcff),
                modifier = Modifier.size(48.dp)
            )
        }
    }

    if(saveDialog){
        val waypointBool = initialWaypoints != waypoints
        val initialDepartureBool = initialDepartureSnapshot != initialDeparture

        if (waypointBool || initialDepartureBool){
            SaveConfirmDialog(
                onResult = { save ->
                    onBack(save)
                    saveDialog = false
                }
            )
        }
        else{
            saveDialog = false
            onBack(false)
        }
    }


    BottomSheet(
        state = sheetState,
        modifier = Modifier
            .shadow(4.dp, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .background(Color.White)
            .widthIn(max = 640.dp)
            .fillMaxWidth()
            .imePadding()
    ) {
        Box(modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                    isFocused = false
                })
            }
            .navigationBarsPadding()
            .imePadding()
            .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .padding(vertical = 2.dp)
                        .size(width = 40.dp, height = 4.dp)
                        .background(color = Color.LightGray, shape = RoundedCornerShape(50))
                        .align(Alignment.CenterHorizontally)
                )
                if (ifAutocomplete) {
                    Autocomplete(
                        resolveAndSuggest = { query, onResult ->
                            planMap.resolveAndSuggest(query = query, onResult = onResult)
                        },
                        address = planWaypointPair?.second?.searchResult?.place?.address?.freeformAddress
                            ?: "",
                        onDone = { searchResult ->
                            // If the planWaypointPair.second (AKA the searchResult) is null, were adding so we call addWaypoint
                            if (planWaypointPair?.second == null) {
                                viewModel.addPlanWaypoint(searchResult)
                            } else { // else were updating a waypoints location
                                viewModel.updateSearchResult(
                                    index = planWaypointPair!!.first,
                                    newSearchResult = searchResult
                                )
                            }
                            ifAutocomplete = false
                            routeFlag.value =
                                false // make the route flag false to let the RouteEditPage that the route function needs to be called
                        },
                        onBack = {
                            ifAutocomplete = false
                        },
                        isTyping = {isFocused = true},
                        findPlaces = { autcompleteResult, placesViewModel ->
                            planMap.findPlaces(
                                result = autcompleteResult,
                                placesViewModel = placesViewModel
                            )
                        },
                        placesViewModel = placesViewModel
                    )
                } else if (routeLegInfo) {
                    sheetState.currentDetent = half
                    RouteLegInfo(
                        selectedRoutePair = selectedRoutePair,
                        onBack = {
                            routeLegInfo = false
                            planMap.onRouteLegClick(
                                route = selectedRoutePair?.first!!,
                                viewModel = viewModel
                            )
                        }
                    )
                } else if (showWaypointSuggestions) {
                    // Only once, zoom on the nearby places markers
                    LaunchedEffect(places) {
                        if (places.isNotEmpty()) {
                            planMap.zoomOnNearbyMarkers(places)
                            planMap.showNearbyMarkers(places, placesViewModel)
                        }
                    }

                    LaunchedEffect(placesViewModel.selectedPlace.value) {
                        if (placesViewModel.selectedPlace.value != null) {
                            sheetState.jumpTo(half)
                        }
                    }
                    // Shows and updates markers based on the location the user clicking to see
                    PlaceListPage(
                        placeList = places,
                        onPlaceClick = { selectedPlace ->
                            planMap.resolveAndSuggest(
                                selectedPlace.name + " " + selectedPlace.address,
                                onResult = { result ->
                                    viewModel.addPlanWaypoint(result.first().second as SearchResult)
                                })
                            showWaypointSuggestions = false
                            locationDetails = false
                            sheetState.jumpTo(half)
                            planMap.removeMarkers(placesViewModel)
                            placesViewModel.updateSelectedPlace(null)
                            viewModel.updateSelectedWaypoint(null)
                        },
                        onBack = {
                            showWaypointSuggestions = false
                            planMap.removeMarkers(placesViewModel)
                            placesViewModel.updateSelectedPlace(null)
                        },
                        placesViewModel = placesViewModel,
                        onZoomOnPlace = { suggPlace ->
                            // zoom into the singular marker
                            planMap.zoomOnNearbyMarker(suggPlace, placesViewModel)
                        }
                    )
                } else if (locationDetails) {
                    LocationDetails(
                        placesViewModel = placesViewModel,
                        location = selectedWaypoint?.searchResult!!,
                        onBack = {
                            locationDetails = false
                            viewModel.updateSelectedWaypoint(null)
                        },
                        planMap = planMap,
                        brandsAndPOIOnly = { query, location, onResult ->
                            planMap.brandsAndPOIOnly(query, location, onResult)
                        },
                        isTyping = {isFocused = true}
                    )
                } else {
                    PlanATripWaypoints(
                        viewModel = viewModel,
                        onRoute = { viewModel ->
                            planMap.clearMap()
                            planMap.planOnRouteRequest(viewModel)
                        },
                        onPlanWaypointEdit = { pair ->
                            //when user clicks on a waypoint they want to edit, RouteEditPage returns a
                            // pair of the index and the waypoint at said index
                            planWaypointPair = pair // Store it in the waypoint
                            ifAutocomplete = true
                        },
                        onPlanWaypointAdd = {
                            planWaypointPair = Pair(
                                0,
                                null
                            )// Make the waypoint pair a null to send to Autocomplete
                            ifAutocomplete = true
                        },
                        isTyping = {isFocused = true},
                        planMap = planMap
                    )
                }
            }
        }
    }
}

@Composable
fun SaveConfirmDialog(
    onResult: (Boolean) -> Unit,
    onDismiss: () -> Unit = { onResult(false) }
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title   = { Text("Save changes?") },
        text    = { Text("Would you like to save your trip before leaving?") },
        confirmButton = {
            TextButton(
                onClick = { onResult(true) }
            ) { Text("Yes") }
        },
        dismissButton = {
            TextButton(
                onClick = { onResult(false) }
            ) { Text("No") }
        }
    )
}
