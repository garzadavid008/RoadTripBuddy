package com.example.roadtripbuddy

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.composed
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import com.google.firebase.components.Lazy
import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.routing.options.ItineraryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

//Compose for the Search/Route page
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchDrawer(
    visible: Boolean, // added parameter to control visibility
    destinationList: MutableList<String>, //Keeps track of the users destination list
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    performSearch: (String, MutableState<String>) -> Unit,//Function Parameter
    resolveAndSuggest: (String, (List<String>) -> Unit) -> Unit,//Function Parameter
    onRouteRequest: (MutableList<String>, MutableState<String>) -> Unit,//Function Parameter
    clearMap: () -> Unit //Function Parameter
) {
    val sheetState = rememberModalBottomSheetState()
    var query by rememberSaveable { mutableStateOf("") } // Keeps track of the users search query
    var suggestions by rememberSaveable { mutableStateOf<List<String>>(emptyList()) } // List of dynamic autocomplete results
    var expanded by rememberSaveable { mutableStateOf(false) }
    var showDetails by rememberSaveable { mutableStateOf(false) } // Boolean for the LocationDetailsPage, if true it displays said compose
    var showRoutePage by rememberSaveable { mutableStateOf(false) }//Boolean for the RouteEditPage, if true it displays said compose
    var selectedLocation by rememberSaveable { mutableStateOf("") }//Keeps track of users initial search that's inputted in LocationDetailsPage, needed for RouteEditPage
    var etaState = remember { mutableStateOf("") }
    var initialRouteSet  = rememberSaveable { mutableStateOf(false) }
    var isPageReady = rememberSaveable { mutableStateOf(false) }

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
                    isRouteReady = isPageReady,
                    initialETA = etaState,
                    onBack = {
                        showDetails = false
                        clearMap()
                        etaState = mutableStateOf("")
                        isPageReady = mutableStateOf(false)
                    },
                    onRouteClick = { // User clicks route button, takes them to RoutEditPage
                        showDetails = false
                        showRoutePage = true
                    },
                )

            } else if (showRoutePage){
                RouteEditPage(
                    initialDestination = selectedLocation,
                    initialRouteSet = initialRouteSet,
                    destinationList = destinationList,
                    onBack = {
                        showRoutePage = false
                        clearMap()
                    },
                    onRoute = {list, eta ->
                        clearMap()
                        onRouteRequest(list, eta)},
                    performAutocomplete = resolveAndSuggest
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
                            performSearch(searchQuery, etaState)
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
                                            performSearch(query, etaState)
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


@Composable
fun LocationDetailsPage(
    locationName: String,
    isRouteReady: MutableState<Boolean>,
    initialETA: MutableState<String>,
    onBack: () -> Unit,
    onRouteClick: () -> Unit
) {
    val ETA = initialETA.value
    LaunchedEffect(ETA) { // This ensures it updates when ETA changes
        if (ETA.isNotEmpty()) {
            isRouteReady.value = true
        }
    }


    if (isRouteReady.value) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            // Back button
            Box(
                modifier = Modifier
                    .clickable { onBack() }
                    .padding(bottom = 16.dp)
            ) {
                Text("← Back", color = Color.Blue)
            }

            // Location details
            Text(locationName, style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Address: 123 Example Street", style = MaterialTheme.typography.bodyMedium)
            Text("City: Sample City", style = MaterialTheme.typography.bodyMedium)
            Text("Category: Landmark", style = MaterialTheme.typography.bodyMedium)

            Spacer(modifier = Modifier.height(24.dp))

            // Route button
            Button(
                onClick = {
                    onRouteClick()
                    isRouteReady.value = false
                          },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6ACFFF),
                    contentColor = Color.White
                )
            ){
                Text(ETA)
            }
        }
    }
}

@Composable
fun RouteEditPage(
    initialDestination: String,
    initialRouteSet: MutableState<Boolean>,
    destinationList: MutableList<String>,
    onBack: () -> Unit,
    onRoute: (MutableList<String>, MutableState<String>) -> Unit,
    performAutocomplete: (String, (List<String>) -> Unit) -> Unit
) {

    val waypoints = remember { mutableStateListOf<String>()}
    val ETA = remember { mutableStateOf("") }

    // Initialize waypoints only once
    LaunchedEffect(Unit) {
        if (waypoints.isEmpty()) {
            waypoints.addAll(
                destinationList.ifEmpty { listOf(initialDestination) }
            )
        }
    }

    val focusManager = LocalFocusManager.current

    // Function to update the route and trigger onRoute
    fun updateRoute() {
        val fullRoute = mutableListOf<String>().apply {
            addAll(waypoints.filter { it.isNotBlank() })
        }
        onRoute(fullRoute, ETA) // Call onRoute to process the route
    }


    //This makes sure the route isn't redrawn when the user closes and reopens the searchDrawer
    LaunchedEffect(Unit) {
        if (!initialRouteSet.value) {
            updateRoute()
        }
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
            text = ETA.value, // ETA is your MutableState<String>
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
                value = "startLocation",
                onValueChange = {},
                label = { Text("Start") },
                enabled = false,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text("Waypoints:")
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                itemsIndexed(
                    items = waypoints,
                    key = { index, waypoint -> waypoint.hashCode() + index } //Making a specific key for each destination
                ) { index, waypoint ->
                    var query by rememberSaveable { mutableStateOf(waypoint) }
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
                                    waypoints[index] = query
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
                                        waypoints[index] = query
                                        updateRoute()
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
                                    waypoints.removeAt(index)
                                    updateRoute()
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
                                            waypoints[index] = suggestion
                                            Log.d("RouteEditPage", waypoints.toString())
                                            suggestions = emptyList()
                                            updateRoute()
                                            focusManager.clearFocus()
                                        }
                                        .padding(8.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (waypoints.size < 20) { //Limiting the amount of waypoints user can add for now, change it for testing
                    item {
                        Button( //ADD WAYPOINT BUTTON
                            onClick = { waypoints.add("") },
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