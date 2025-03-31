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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.roadtripbuddy.SearchManager
import com.example.roadtripbuddy.TripViewModel
import kotlinx.coroutines.delay

//Compose for the Search/Route page
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchDrawer(
    modifier: Modifier = Modifier,
    viewModel: TripViewModel = viewModel(),
    visible: Boolean, // added parameter to control visibility
    onDismiss: () -> Unit,
    performSearch: (String, TripViewModel) -> Unit,//Function Parameter
    resolveAndSuggest: (String, (List<String>) -> Unit) -> Unit,//Function Parameter
    onRouteRequest: (TripViewModel) -> Unit,//Function Parameter
    clearMap: () -> Unit ,//Function Parameter
    searchManager: SearchManager,
    onStartTrip: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    var query by rememberSaveable { mutableStateOf("") } // Keeps track of the users search query
    var suggestions by rememberSaveable { mutableStateOf<List<String>>(emptyList()) } // List of dynamic autocomplete results
    var expanded by rememberSaveable { mutableStateOf(false) }
    var showDetails by rememberSaveable { mutableStateOf(false) } // Boolean for the LocationDetailsPage, if true it displays said compose
    var showRoutePage by rememberSaveable { mutableStateOf(false) }//Boolean for the RouteEditPage, if true it displays said compose
    var selectedLocation by rememberSaveable { mutableStateOf("") }//Keeps track of users initial search that's inputted in LocationDetailsPage, needed for RouteEditPage
    var isPageReady = rememberSaveable { mutableStateOf(false) }
    val initialRouteSet  = rememberSaveable { mutableStateOf(false) }

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
                    locationName = selectedLocation,
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
                    },
                )

            } else if (showRoutePage) {
                RouteEditPage(
                    viewModel = viewModel,
                    initialDestination = selectedLocation,
                    initialRouteSet = initialRouteSet,
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
                    performAutocomplete = resolveAndSuggest,
                    searchManager = searchManager
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
                            performSearch(searchQuery, viewModel)
                            selectedLocation = searchQuery
                            showDetails = true
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
                                suggestions = initSuggestions.distinct()
                            }
                        } else {
                            suggestions = emptyList()
                        }
                    }

                    if (query.isNotEmpty()) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                                .background(Color.White)
                        ) {
                            items(suggestions) { suggestion ->
                                Text(
                                    text = suggestion,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            query = suggestion
                                            selectedLocation = query
                                            expanded = false
                                            performSearch(query, viewModel)
                                            showDetails = true
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

