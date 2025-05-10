package com.example.roadtripbuddy.SearchDrawer

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.roadtripbuddy.PlacesViewModel
import com.example.roadtripbuddy.PlanMap
import com.tomtom.sdk.location.ExperimentalPoiInPlaceApi
import com.tomtom.sdk.routing.route.Route
import com.tomtom.sdk.search.common.error.SearchFailure
import com.tomtom.sdk.search.model.SearchResultType
import com.tomtom.sdk.search.model.result.AutocompleteResult
import com.tomtom.sdk.search.model.result.AutocompleteSegmentBrand
import com.tomtom.sdk.search.model.result.AutocompleteSegmentPoiCategory
import com.tomtom.sdk.search.model.result.SearchResult
import kotlinx.coroutines.delay

@OptIn(ExperimentalPoiInPlaceApi::class)
@Composable
fun LocationDetails(
    placesViewModel: PlacesViewModel,
    location: SearchResult,
    onBack: () -> Unit,
    planMap: PlanMap,
    brandsAndPOIOnly: (String, SearchResult, (List<Pair<String, AutocompleteResult>>) -> Unit) -> Unit,
    isTyping: () -> Unit,
    categoryReturn: (String) -> Unit
    ) {

    var address by remember { mutableStateOf(location.place.address?.freeformAddress) }

    LaunchedEffect(location) {
        if (location.type == SearchResultType.Poi) {
            planMap.searchManager.toPoi(location.searchResultId) { poiResult ->
                // safely grab first POI name
                val poiName = poiResult
                    ?.poiDetails
                    ?.poi
                    ?.names
                    ?.firstOrNull()
                    .orEmpty()

                if (poiName.isNotBlank()) {
                    address = poiName
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        // Back button
        IconButton(onClick = { onBack() }) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color(0xFF2ebcff)
            )
        }
        // Location details
        Text(
            "Search Nearby ${address}",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(16.dp))


        var autocompleteSuggestions by remember {
            mutableStateOf<List<Pair<String, AutocompleteResult>>>(
                emptyList()
            )
        }
        var query by remember { mutableStateOf("") }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { newQuery ->
                    query = newQuery
                },
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        brandsAndPOIOnly(query, location) { results ->
                            val searchResult = results.first().second
                            planMap.findPlaces(searchResult, placesViewModel)
                        }
                    }
                ),
                label = { Text(text = "Enter a location") },
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
                    .onKeyEvent { keyEvent -> // For testing basically, makes the Enter button on our keyboards commit a waypoint[index] change
                        if (keyEvent.type == KeyEventType.KeyUp && keyEvent.key == Key.Enter) {
                            brandsAndPOIOnly(query, location) { results ->
                                val searchResult = results.first().second
                                planMap.findPlaces(searchResult, placesViewModel)
                            }
                            true
                        } else {
                            false
                        }
                    }
                    .onFocusChanged { focusState ->
                        if (focusState.isFocused) {
                            isTyping()
                        }
                    }
            )
        }

        LaunchedEffect(query) { //Pulsing the API call for autocomplete
            if (query.isNotEmpty()) {
                delay(300)
                brandsAndPOIOnly(query, location) { initSuggestions ->
                    autocompleteSuggestions = initSuggestions.distinct()
                }
            } else {
                autocompleteSuggestions = emptyList()
            }
        }

        if (autocompleteSuggestions.isNotEmpty()) {
            Log.d("Autocomplete suggestions", autocompleteSuggestions.toString())
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(items = autocompleteSuggestions) { suggestionPair ->
                    val (suggestion, objectResult) = suggestionPair
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                planMap.findPlaces(objectResult, location = location.place.coordinate, placesViewModel = placesViewModel)
                                val brandName =
                                    objectResult.segments.filterIsInstance<AutocompleteSegmentBrand>().firstOrNull()?.brand?.name
                                val poiName = objectResult.segments.filterIsInstance<AutocompleteSegmentPoiCategory>().firstOrNull()?.poiCategory?.name
                                categoryReturn((brandName ?: poiName).toString())
                            }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = suggestion, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
    }
}