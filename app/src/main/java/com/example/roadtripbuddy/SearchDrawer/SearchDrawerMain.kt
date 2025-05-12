package com.example.roadtripbuddy.SearchDrawer

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
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
import com.example.roadtripbuddy.SuggPlace
import com.example.roadtripbuddy.pages.PlaceListPage
import com.tomtom.sdk.search.model.SearchResultType
import com.tomtom.sdk.search.model.result.AutocompleteSegmentBrand
import com.tomtom.sdk.search.model.result.AutocompleteSegmentPoiCategory
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
    onStartTrip: () -> Unit,
    ifSuggestionPlace: SuggPlace? = null,
) {
    val context = LocalContext.current
    var showDetails by rememberSaveable { mutableStateOf(false) } // Boolean for the LocationDetailsPage, if true it displays said compose
    var showRoutePage by rememberSaveable { mutableStateOf(false) }//Boolean for the RouteEditPage, if true it displays said compose
    var isPageReady = rememberSaveable { mutableStateOf(false) }
    val routeFlag  = rememberSaveable { mutableStateOf(false) }
    var ifAutocomplete by remember { mutableStateOf(false) }
    var waypointPair by remember { mutableStateOf<Pair<Int,SearchResult?>?>(null) } // For the autocomplete composable page,
    var searchPage by rememberSaveable { mutableStateOf(false) }
    var emptyPage by rememberSaveable { mutableStateOf(false) }
    var noLocationPage by rememberSaveable { mutableStateOf(false) }
    //we store an index and a waypoint from said index from the RouteEditPage in here in order to send it to the autocomplete page

    val focusManager =  LocalFocusManager.current
    var isFocused by remember { mutableStateOf(false) }

    var showSuggestions by rememberSaveable { mutableStateOf(false) }
    var showWaypointSuggestions by rememberSaveable { mutableStateOf(false) }
    val places by placesViewModel.places.collectAsState()

    var category by remember { mutableStateOf("") }

    val selectedLocation by viewModel.selectedLocation

    fun resetDrawer(){
        showDetails = false
        showRoutePage = false
        ifAutocomplete = false
        showSuggestions = false
        showWaypointSuggestions = false
        viewModel.updateSelectedLocation(null)
        viewModel.clearWaypoints()
        navMap.clearMap()
        navMap.removeMarkers(placesViewModel)
        placesViewModel.updateSelectedPlace(null)
    }

    val selectedPlace = places.firstOrNull()

    val peek = SheetDetent(identifier = "peek") { containerHeight, sheetHeight ->
        containerHeight * 0.1f
    }

    val half = SheetDetent(identifier = "half") { containerHeight, sheetHeight ->
        containerHeight * 0.5f
    }

    val sheetState = rememberBottomSheetState(
        initialDetent = peek,
        detents = listOf(peek, half, FullyExpanded)
    )

    LaunchedEffect(navMap.searchManager.startLocation) {
        if (navMap.searchManager.startLocation == null){
            noLocationPage = true
        }
        else {
            noLocationPage = false
        }
    }

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
        if (!searchPage && sheetState.currentDetent == peek){
            emptyPage = true
        } else{
            emptyPage = false
        }
        if(sheetState.currentDetent != FullyExpanded){
            focusManager.clearFocus()
            isFocused = false
        }
    }

    LaunchedEffect(ifSuggestionPlace) {
        if (ifSuggestionPlace != null){
            resetDrawer()
            Log.d("ifSuggestionPlace", ifSuggestionPlace.name)
            navMap.resolveAndSuggest(
                query = ifSuggestionPlace.name + " " + ifSuggestionPlace.address,
                isPoi = setOf(SearchResultType.Poi),
                onResult = { searchResult ->
                    if (searchResult.isNotEmpty()){
                        viewModel.updateSelectedLocation(searchResult.first().second as SearchResult)
                        navMap.performSearch(ifSuggestionPlace.address, viewModel)
                        showDetails = true
                    } else {
                        Toast
                            .makeText(
                                context,
                                "Location Failure",
                                Toast.LENGTH_LONG
                            )
                            .show()
                    }
                }
            )
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
                if (emptyPage){
                    emptyPage()
                }
                if (showDetails) {
                    if (selectedLocation != null){
                        searchPage = false
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
                    } else {
                        Toast
                            .makeText(
                                context,
                                "Location Not Available",
                                Toast.LENGTH_LONG
                            )
                            .show()
                    }

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
                            routeFlag.value = false
                            sheetState.jumpTo(peek)
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
                    searchPage = false
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
                            navMap.resolveAndSuggest(
                                query = selectedPlace.name + " " + selectedPlace.address,
                                isPoi = setOf(SearchResultType.Poi),
                                onResult = { searchResult ->
                                    if (searchResult.isNotEmpty()){
                                        viewModel.updateSelectedLocation(searchResult.first().second as SearchResult)
                                        navMap.performSearch(selectedPlace.address, viewModel)
                                        showDetails = true
                                        showSuggestions = false
                                    } else {
                                        Toast
                                            .makeText(
                                                context,
                                                "Location Failure",
                                                Toast.LENGTH_LONG
                                            )
                                            .show()
                                    }
                                }
                            )
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
                        },
                        category = category
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
                            navMap.resolveAndSuggest(
                                query = selectedPlace.name + " " + selectedPlace.address,
                                isPoi = setOf(SearchResultType.Poi),
                                onResult = { searchResult ->
                                    if (searchResult.isNotEmpty()){
                                        viewModel.updateSelectedLocation(searchResult.first().second as SearchResult)
                                        if (waypointPair?.second == null) {
                                            viewModel.addWaypoint(selectedLocation!!){
                                                Toast.makeText(context, "Waypoint already exists", Toast.LENGTH_SHORT).show()
                                            }
                                        } else { // else were updating a waypoint
                                            viewModel.updateWaypoint(
                                                index = waypointPair!!.first,
                                                newValue = selectedLocation!!
                                            ){
                                                Toast.makeText(context, "Waypoint already exists", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                        ifAutocomplete = false
                                        showWaypointSuggestions = false
                                        showRoutePage = true
                                    }else {
                                        Toast
                                            .makeText(
                                                context,
                                                "Location Failure",
                                                Toast.LENGTH_LONG
                                            )
                                            .show()
                                    }
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
                        },
                        category = category
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
                                viewModel.addWaypoint(searchResult){
                                    Toast.makeText(context, "Waypoint already exists", Toast.LENGTH_SHORT).show()
                                }
                            } else { // else were updating a waypoint
                                viewModel.updateWaypoint(
                                    index = waypointPair!!.first,
                                    newValue = searchResult
                                ){
                                    Toast.makeText(context, "Waypoint already exists", Toast.LENGTH_SHORT).show()
                                }
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
                            val brandName =
                                autocompleteResult.segments.filterIsInstance<AutocompleteSegmentBrand>().firstOrNull()?.brand?.name
                            val poiName = autocompleteResult.segments.filterIsInstance<AutocompleteSegmentPoiCategory>().firstOrNull()?.poiCategory?.name
                            category = (brandName ?: poiName).toString()
                        }
                    )
                } else if (noLocationPage){
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.TopCenter // this centers the child content
                    ) {
                        Text("Location must be on to use navigation")
                    }
                }
                else {
                    searchPage = true
                    SearchDrawerAutocomplete(
                        navMap = navMap,
                        placesViewModel = placesViewModel,
                        searchDrawerViewModel = viewModel,
                        onDone = { searchResult ->
                            viewModel.updateSelectedLocation(searchResult)
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
                        isTyping = { isFocused = true},
                        category = { result ->
                            category = result
                        }
                    )
                }

            }
        }
    }
}

@Composable
fun emptyPage(){
    Box(Modifier.fillMaxSize()){

    }
}

