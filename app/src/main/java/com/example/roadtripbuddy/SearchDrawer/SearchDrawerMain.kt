package com.example.roadtripbuddy.SearchDrawer

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.composables.core.BottomSheet
import com.composables.core.SheetDetent
import com.composables.core.SheetDetent.Companion.FullyExpanded
import com.composables.core.SheetDetent.Companion.Hidden
import com.composables.core.rememberBottomSheetState
import com.example.roadtripbuddy.Autocomplete
import com.example.roadtripbuddy.NavigationMap
import com.example.roadtripbuddy.PlacesViewModel
import com.example.roadtripbuddy.SearchDrawerViewModel
import com.example.roadtripbuddy.SearchManager
import com.example.roadtripbuddy.pages.PlaceListPage
import com.tomtom.sdk.search.model.result.SearchResult


//Compose for the Search/Route page
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchDrawer(
    modifier: Modifier = Modifier,
    viewModel: SearchDrawerViewModel,
    placesViewModel: PlacesViewModel,
    navMap: NavigationMap,
    visible: Boolean, // added parameter to control visibility
    onDismiss: () -> Unit,
    searchManager: SearchManager,
    onStartTrip: () -> Unit
) {
    var showDetails by rememberSaveable { mutableStateOf(false) } // Boolean for the LocationDetailsPage, if true it displays said compose
    var showRoutePage by rememberSaveable { mutableStateOf(false) }//Boolean for the RouteEditPage, if true it displays said compose
    var selectedLocation by rememberSaveable { mutableStateOf<SearchResult?>(null) }//Keeps track of users initial search that's inputted in LocationDetailsPage, needed for RouteEditPage
    var isPageReady = rememberSaveable { mutableStateOf(false) }
    val routeFlag  = rememberSaveable { mutableStateOf(false) }
    var ifAutocomplete by remember { mutableStateOf(false) }
    var waypointPair by remember { mutableStateOf<Pair<Int,SearchResult?>?>(null) } // For the autocomplete composable page,
    //we store an index and a waypoint from said index from the RouteEditPage in here in order to send it to the autocomplete page

    val focusManager =  LocalFocusManager.current
    var isFocused by remember { mutableStateOf(false) }

    var showSuggestions by rememberSaveable { mutableStateOf(false) }
    var showWaypointSuggestions by rememberSaveable { mutableStateOf(false) }
    val places by placesViewModel.restaurants.collectAsState()


    val selectedPlace = places.firstOrNull()

    val peek = SheetDetent(identifier = "peek") { containerHeight, sheetHeight ->
        containerHeight * 0.1f
    }

    val half = SheetDetent(identifier = "half") { containerHeight, sheetHeight ->
        containerHeight * 0.5f
    }

    val sheetState = rememberBottomSheetState(
        initialDetent = peek,
        detents = listOf(peek, half, FullyExpanded, Hidden)
    )

    LaunchedEffect(places) {
        Log.d("Chris", "places updated: ${places.size}")
        if (places.isNotEmpty() && places.size>1) {
            if (ifAutocomplete){
                showWaypointSuggestions = true
            }else {
                showSuggestions = true
            }
            sheetState.jumpTo(half)
        }
    }

    LaunchedEffect(isFocused) {
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


    BottomSheet(
        state = sheetState,
        modifier = Modifier
            .shadow(4.dp, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .background(Color.White)
            .widthIn(max = 640.dp)
            .fillMaxWidth()
            .padding(
                // make sure the sheet is not behind nav bars on landscape
                WindowInsets.navigationBars.only(WindowInsetsSides.Horizontal)
                    .asPaddingValues()
            )
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

                if (showDetails) {
                    LocationDetailsPage(
                        location = selectedLocation!!,
                        viewModel = viewModel,
                        place = selectedPlace,
                        isRouteReady = isPageReady,
                        onBack = {
                            showDetails = false
                            navMap.clearMap()
                            viewModel.updateETA("")
                            isPageReady = mutableStateOf(false)
                        },
                        onRouteClick = { // User clicks route button, takes them to RoutEditPage
                            showDetails = false
                            showRoutePage = true
                            viewModel.initializeWaypoints(selectedLocation!!)
                        },
                        navMap = navMap
                    )

                } else if (showRoutePage) {
                    navMap.nearbyMarkerClickListener?.let { navMap.removeMarkerClickListener(it) }
                    RouteEditPage(
                        viewModel = viewModel,
                        routeFlag = routeFlag,
                        onBack = {
                            showRoutePage = false
                            navMap.clearMap()
                            viewModel.clearWaypoints()
                        },
                        onRoute = { vm ->
                            navMap.clearMap()
                            navMap.onRouteRequest(vm, navMap.context)
                        },
                        onStartTrip = {
                            onDismiss()            //  hides the drawer
                            showRoutePage = false  // hides RouteEditPage's view
                            sheetState.jumpTo(Hidden)
                            onStartTrip() // calls navigationMap.startTrip() from the parent
                        },
                        searchManager = searchManager,
                        onWaypointEdit = { pair ->
                            //when user clicks on a waypoint they want to edit, RouteEditPage returns a
                            // pair of the index and the waypoint at said index
                            waypointPair = pair // Store it in the waypoint
                            showRoutePage = false
                            ifAutocomplete = true
                        },
                        onWaypointAdd = {
                            waypointPair =
                                Pair(
                                    0,
                                    null
                                )// Make the waypoint pair a null to send to Autocomplete
                            showRoutePage = false
                            ifAutocomplete = true
                        }
                    )
                } else if (showSuggestions) {
                    // Only once, zoom on the nearby places markers
                    LaunchedEffect(places) {
                        if (places.isNotEmpty()) {
                            navMap.zoomOnNearbyMarkers(places)
                            navMap.showNearbyMarkers(places, placesViewModel)
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
                            navMap.removeMarkers(placesViewModel)
                            navMap.clearMap()
                            navMap.resolveAndSuggest(selectedPlace.name + " " + selectedPlace.address, onResult = { result ->
                                selectedLocation = result.first().second as SearchResult
                                navMap.performSearch(selectedPlace.address, viewModel)
                                showSuggestions = false
                                showDetails = true
                            })
                            sheetState.jumpTo(half)
                            placesViewModel.updateSelectedPlace(null)
                        },
                        onBack = {
                            showSuggestions = false
                            navMap.removeMarkers(placesViewModel)
                            placesViewModel.updateSelectedPlace(null)
                        },
                        placesViewModel = placesViewModel,
                        onZoomOnPlace = { suggPlace ->
                            // zoom into the singular marker
                            navMap.zoomOnNearbyMarker(suggPlace, placesViewModel)
                        }
                    )
                } else if (showWaypointSuggestions) {
                    // Only once, zoom on the nearby places markers
                    LaunchedEffect(places) {
                        if (places.isNotEmpty()) {
                            navMap.clearMap()
                            navMap.zoomOnNearbyMarkers(places)
                            navMap.showNearbyMarkers(places, placesViewModel)
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
                            navMap.removeMarkers(placesViewModel)
                            navMap.clearMap()
                            navMap.resolveAndSuggest(selectedPlace.name + " " + selectedPlace.address, onResult = { result ->
                                selectedLocation = result.first().second as SearchResult
                                if (waypointPair?.second == null) {
                                    viewModel.addWaypoint(selectedLocation!!)
                                } else { // else were updating a waypoint
                                    viewModel.updateWaypoint(
                                        index = waypointPair!!.first,
                                        newValue = selectedLocation!!
                                    )
                                }
                                showWaypointSuggestions = false
                                showRoutePage = true
                            })
                            if (sheetState.currentDetent != half) {
                                sheetState.jumpTo(half)
                            }
                            placesViewModel.updateSelectedPlace(null)
                            routeFlag.value = false
                        },
                        onBack = {
                            placesViewModel.updateSelectedPlace(null)
                            showWaypointSuggestions = false
                            navMap.removeMarkers(placesViewModel)
                            routeFlag.value = false
                        },
                        placesViewModel = placesViewModel,
                        onZoomOnPlace = { suggPlace ->
                            // zoom into the singular marker
                            navMap.zoomOnNearbyMarker(suggPlace, placesViewModel)
                        }
                    )
                } else if (ifAutocomplete) {
                    Autocomplete(
                        resolveAndSuggest = { query, onResult ->
                            navMap.resolveAndSuggest(query = query, onResult = onResult)
                        },
                        address = waypointPair?.second?.place?.address?.freeformAddress ?: "",
                        onDone = { searchResult ->
                            // If the waypointPair.second (AKA the searchResult) is null, were adding so we call addWaypoint
                            if (waypointPair?.second == null) {
                                viewModel.addWaypoint(searchResult)
                            } else { // else were updating a waypoint
                                viewModel.updateWaypoint(
                                    index = waypointPair!!.first,
                                    newValue = searchResult
                                )
                            }
                            ifAutocomplete = false
                            showRoutePage = true
                            routeFlag.value =
                                false // make the route flag false to let the RouteEditPage that the route function needs to be called
                        },
                        onBack = {
                            sheetState.jumpTo(half)
                            ifAutocomplete = false
                            showRoutePage = true
                        },
                        isTyping = {isFocused = true},
                        placesViewModel = placesViewModel,
                        findPlaces = { autocompleteResult, placesViewModel ->
                            navMap.findPlaces(result = autocompleteResult, placesViewModel = placesViewModel)

                        }
                    )
                } else {
                    SearchDrawerAutocomplete(
                        navMap = navMap,
                        placesViewModel = placesViewModel,
                        searchDrawerViewModel = viewModel,
                        onDone = { searchResult ->
                            selectedLocation = searchResult
                            /*
                            placesViewModel.getTextSearch(
                                searchResult.place.address?.freeformAddress!!,
                                searchResult.place.coordinate.latitude,
                                searchResult.place.coordinate.longitude
                            )

                             */
                            sheetState.jumpTo(half)
                            showDetails = true
                        },
                        isTyping = { isFocused = true}
                    )
                }

            }
        }
    }
}

