package com.example.roadtripbuddy.SearchDrawer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.roadtripbuddy.NavigationMap
import com.example.roadtripbuddy.PlacesViewModel
import com.example.roadtripbuddy.SearchDrawerViewModel
import com.tomtom.sdk.search.model.result.AutocompleteResult
import com.tomtom.sdk.search.model.result.SearchResult
import kotlinx.coroutines.delay
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme





@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchDrawerAutocomplete(
    navMap: NavigationMap,
    placesViewModel: PlacesViewModel,
    searchDrawerViewModel: SearchDrawerViewModel,
    onDone: (SearchResult) -> Unit,
    isTyping: () -> Unit
) {
    var query by rememberSaveable { mutableStateOf("") } // Keeps track of the users search query
    var autocompleteSuggestions by remember { mutableStateOf<List<Pair<String, Any?>>>(emptyList()) } // List of dynamic autocomplete results
    var expanded by rememberSaveable { mutableStateOf(false) }

    Box(Modifier.fillMaxSize()) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            SearchBarDefaults.InputField(
                query = query,
                onQueryChange = { newQuery ->
                    query = newQuery
                    expanded = true
                },
                onSearch = { searchQuery ->
                    navMap.resolveAndSuggest(query = searchQuery, onResult = { results ->
                        val (address, searchResult) = results.first()
                        if (searchResult is SearchResult) {
                            navMap.performSearch(address, searchDrawerViewModel)
                            onDone(searchResult)
                        } else if (searchResult is AutocompleteResult) {
                            navMap.findPlaces(
                                result = searchResult,
                                placesViewModel = placesViewModel
                            )
                        }
                    })
                },
                expanded = expanded,
                onExpandedChange = { expanded = it },
                placeholder = { Text("Search Location") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null
                    )
                },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = null
                    )
                },
                modifier = Modifier.onFocusChanged { focusState ->
                    if (focusState.isFocused) {
                        isTyping()
                    }
                }
            )

            LaunchedEffect(query) { //Pulsing the API call for autocomplete
                if (query.isNotEmpty()) {
                    delay(300)
                    navMap.resolveAndSuggest(query = query, onResult = { initSuggestions ->
                        autocompleteSuggestions = initSuggestions.distinct()
                    })
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
                    itemsIndexed(autocompleteSuggestions) { index, suggestionPair ->
                        val (suggestion, objectResult) = suggestionPair

                        val shape = when (index) {
                            0 -> RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
                            autocompleteSuggestions.lastIndex -> RoundedCornerShape(
                                bottomStart = 12.dp,
                                bottomEnd = 12.dp
                            )

                            else -> RoundedCornerShape(0.dp)
                        }

                        Column {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(shape)
                                    .background(Color(0xFF2EBCFF), shape)
                                    .clickable {
                                        if (objectResult is AutocompleteResult) {
                                            navMap.findPlaces(objectResult, placesViewModel)
                                        } else {
                                            query = suggestion
                                            expanded = false
                                            navMap.performSearch(query, searchDrawerViewModel)
                                            onDone(objectResult as SearchResult)
                                        }
                                    }
                            ) {
                                Text(
                                    text = suggestion,
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodyLarge,
                                    maxLines = 1,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 12.dp)
                                )
                            }

                            if (index < autocompleteSuggestions.lastIndex) {
                                Divider(
                                    thickness = 0.5.dp,
                                    color = Color.White,
                                    modifier = Modifier.padding(start = 16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}