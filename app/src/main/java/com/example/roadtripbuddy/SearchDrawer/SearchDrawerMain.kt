package com.example.roadtripbuddy.SearchDrawer

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.roadtripbuddy.Autocomplete
import com.example.roadtripbuddy.SearchDrawerViewModel
import com.example.roadtripbuddy.SearchManager
import com.tomtom.sdk.search.model.result.SearchResult
import kotlinx.coroutines.delay


//Compose for the Search/Route page
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchDrawer(
    modifier: Modifier = Modifier,
    viewModel: SearchDrawerViewModel = viewModel(),
    visible: Boolean, // added parameter to control visibility
    onDismiss: () -> Unit,
    performSearch: (String, SearchDrawerViewModel) -> Unit,//Function Parameter
    resolveAndSuggest: (String, (List<Pair<String, Any?>>) -> Unit) -> Unit,//Function Parameter
    onRouteRequest: (SearchDrawerViewModel) -> Unit,//Function Parameter
    clearMap: () -> Unit ,//Function Parameter
    searchManager: SearchManager,
    onStartTrip: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    var query by rememberSaveable { mutableStateOf("") } // Keeps track of the users search query
    var autocompleteSuggestions by rememberSaveable { mutableStateOf<List<Pair<String, Any?>>>(emptyList()) } // List of dynamic autocomplete results
    var expanded by rememberSaveable { mutableStateOf(false) }
    var showDetails by rememberSaveable { mutableStateOf(false) } // Boolean for the LocationDetailsPage, if true it displays said compose
    var showRoutePage by rememberSaveable { mutableStateOf(false) }//Boolean for the RouteEditPage, if true it displays said compose
    var selectedLocation by rememberSaveable { mutableStateOf<SearchResult?>(null) }//Keeps track of users initial search that's inputted in LocationDetailsPage, needed for RouteEditPage
    var isPageReady = rememberSaveable { mutableStateOf(false) }
    val routeFlag  = rememberSaveable { mutableStateOf(false) }
    var ifAutocomplete by remember { mutableStateOf(false) }
    var waypointPair by remember { mutableStateOf<Pair<Int,SearchResult?>?>(null) } // For the autocomplete composable page,
    //we store an index and a waypoint from said index from the RouteEditPage in here in order to send it to the autocomplete page

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
                    isRouteReady = isPageReady,
                    onBack = {
                        showDetails = false
                        clearMap()
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
                        clearMap()
                        viewModel.clearWaypoints()
                    },
                    onRoute = { vm ->
                        clearMap()
                        onRouteRequest(vm)
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
            } else if (ifAutocomplete){
                Autocomplete(
                    resolveAndSuggest = resolveAndSuggest,
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
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    SearchBarDefaults.InputField(
                        query = query,
                        onQueryChange = { newQuery ->
                            query = newQuery
                            expanded = true
                        },
                        onSearch = { searchQuery ->
                            resolveAndSuggest(searchQuery){ results ->
                                val (address, searchResult) = results.first()
                                if (searchResult is SearchResult){
                                    performSearch(address, viewModel)
                                    selectedLocation = searchResult
                                    showDetails = true
                                } else {
                                    // PLaces Nearby
                                }
                            }
                        },
                        expanded = expanded,
                        onExpandedChange = { expanded = it },
                        placeholder = { Text("Search Location") },
                        leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                        trailingIcon = { Icon(imageVector = Icons.Default.MoreVert, contentDescription = null) },
                    )

                    LaunchedEffect(query) { //Pulsing the API call for autocomplete
                        if (query.isNotEmpty()) {
                            delay(300)
                            resolveAndSuggest(query) { initSuggestions ->
                                autocompleteSuggestions= initSuggestions.distinct()
                            }
                        } else {
                            autocompleteSuggestions = emptyList()
                        }
                    }

                    if (query.isNotEmpty()) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                                .background(Color.White)
                        ) {
                            items(items = autocompleteSuggestions) { suggestionPair ->
                                val (suggestion, objectResult) = suggestionPair
                                Text(
                                    text = suggestion,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            if (objectResult is SearchResult){
                                                query = suggestion
                                                selectedLocation = objectResult
                                                expanded = false
                                                performSearch(query, viewModel)
                                                showDetails = true
                                            } else {
                                                // Places Nearby
                                            }
                                        }
                                        .padding(8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

