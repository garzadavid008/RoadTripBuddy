package com.example.roadtripbuddy.SearchDrawer

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.roadtripbuddy.SearchManager
import com.example.roadtripbuddy.pages.PlaceListPage
import com.example.roadtripbuddy.Autocomplete
import com.example.roadtripbuddy.NavigationMap
import com.example.roadtripbuddy.PlacesViewModel
import com.example.roadtripbuddy.SearchDrawerViewModel
import com.tomtom.sdk.search.model.result.AutocompleteResult
import com.tomtom.sdk.search.model.result.SearchResult
import kotlinx.coroutines.delay


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
    val sheetState = rememberModalBottomSheetState()
    var showDetails by rememberSaveable { mutableStateOf(false) } // Boolean for the LocationDetailsPage, if true it displays said compose
    var showRoutePage by rememberSaveable { mutableStateOf(false) }//Boolean for the RouteEditPage, if true it displays said compose
    var selectedLocation by rememberSaveable { mutableStateOf<SearchResult?>(null) }//Keeps track of users initial search that's inputted in LocationDetailsPage, needed for RouteEditPage
    var isPageReady = rememberSaveable { mutableStateOf(false) }
    val routeFlag  = rememberSaveable { mutableStateOf(false) }
    var ifAutocomplete by remember { mutableStateOf(false) }
    var waypointPair by remember { mutableStateOf<Pair<Int,SearchResult?>?>(null) } // For the autocomplete composable page,
    //we store an index and a waypoint from said index from the RouteEditPage in here in order to send it to the autocomplete page

    var showSuggestions by rememberSaveable { mutableStateOf(false) }
    val places by placesViewModel.restaurants.collectAsState()

    LaunchedEffect(places) {
        Log.d("Chris", "places updated: ${places.size}")
        if (places.isNotEmpty() && places.size>1) {

            Log.d("Chris", "places updatedAA: ${places.size}")
            showSuggestions = true
            showDetails= false
        }
    }

    val selectedPlace = places.firstOrNull()


    //AnimatedVisibility keeps state when the SearchDrawer is dismissed/not displayed
    AnimatedVisibility(visible = visible) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            modifier = modifier,
            scrimColor = Color.Transparent
        ) {
            if (showDetails) {
                LocationDetailsPage(
                    locationName = selectedLocation?.place?.address?.freeformAddress!!,
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
                )

            } else if (showRoutePage) {
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
                        navMap.onRouteRequest(vm)
                    },
                    onStartTrip = {
                        onDismiss()            //  hides the drawer
                        showRoutePage = false  // hides RouteEditPage's view
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
                        waypointPair = Pair(0, null)// Make the waypoint pair a null to send to Autocomplete
                        showRoutePage = false
                        ifAutocomplete = true
                    }
                )
            } else if(showSuggestions)
            {
                PlaceListPage(
                    placeList = places,
                    onPlaceClick  = { selectedPlace ->
                        navMap.clearMap()
                        navMap.resolveAndSuggest(selectedPlace.address, onResult = { result ->
                            selectedLocation = result.first().second as SearchResult
                            navMap.performSearch(selectedPlace.address, viewModel)
                            showSuggestions = false
                            showDetails = true
                        })
                    },
                    onBack = {
                        showSuggestions = false
                        navMap.clearMap()
                             },
                    onCameraMove = { navMap.showNearbyMarkers(searchManager.startLocation!!)}
                )
            } else if(ifAutocomplete) {
                Autocomplete(
                    resolveAndSuggest = { query, onResult ->
                        navMap.resolveAndSuggest(query = query, onResult = onResult)
                    },
                    address = waypointPair?.second?.place?.address?.freeformAddress ?: "",
                    onDone = { searchResult ->
                        // If the waypointPair.second (AKA the searchResult) is null, were adding so we call addWaypoint
                        if (waypointPair?.second == null){
                            viewModel.addWaypoint(searchResult)
                        } else{ // else were updating a waypoint
                            viewModel.updateWaypoint(
                                index = waypointPair!!.first,
                                newValue = searchResult
                            )
                        }
                        ifAutocomplete = false
                        showRoutePage = true
                        routeFlag.value = false // make the route flag false to let the RouteEditPage that the route function needs to be called
                    },
                    onBack = {
                        ifAutocomplete = false
                        showRoutePage = true
                    }
                )
            }
            else
            {
                SearchDrawerAutocomplete(
                    navMap = navMap,
                    placesViewModel = placesViewModel,
                    searchDrawerViewModel = viewModel,
                    onDone = {searchResult ->
                        selectedLocation = searchResult
                        placesViewModel.getTextSearch(
                            searchResult.place.address?.freeformAddress!!,
                            searchResult.place.coordinate.latitude,
                            searchResult.place.coordinate.longitude
                        )
                        showDetails = true
                    }
                )
            }
        }
    }
}

