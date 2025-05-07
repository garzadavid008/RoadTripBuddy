package com.example.roadtripbuddy

import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import com.tomtom.sdk.search.model.result.AutocompleteResult
import com.tomtom.sdk.search.model.result.SearchResult
import kotlinx.coroutines.delay


@Composable
fun Autocomplete(
    modifier: Modifier = Modifier,
    placesViewModel: PlacesViewModel,
    address: String = "",
    resolveAndSuggest: (String, (List<Pair<String, Any?>>) -> Unit) -> Unit,
    onDone: (SearchResult) -> Unit,
    onBack: () -> Unit,
    isTyping: () -> Unit,
    findPlaces: (AutocompleteResult, PlacesViewModel) -> Unit
) {
    // The results returned by our "autocomplete" AKA resolveAndSuggest method in searchManager, we store
    // them in here
    var autocompleteSuggestions by remember { mutableStateOf<List<Pair<String, Any?>>>(emptyList()) }
    var query by remember { mutableStateOf(address) }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = modifier.fillMaxWidth()){
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Back"

                    )
                }
                OutlinedTextField(
                    value = query,
                    onValueChange = { newQuery ->
                        query = newQuery
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if(query.isBlank()||query.isEmpty()){
                                return@KeyboardActions
                            }
                            resolveAndSuggest(query){ results ->
                                val searchResult= results.first().second
                                if (searchResult is SearchResult){
                                    onDone(searchResult)
                                } else if (searchResult is AutocompleteResult){
                                    findPlaces(searchResult, placesViewModel)
                                }
                            }
                        }
                    ),
                    label = { Text(text = "Enter a location") },
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp)
                        .onKeyEvent { keyEvent -> // For testing basically, makes the Enter button on our keyboards commit a waypoint[index] change
                            if (keyEvent.type == KeyEventType.KeyUp && keyEvent.key == Key.Enter) {
                                resolveAndSuggest(query){ results ->
                                    val searchResult= results.first().second
                                    if (searchResult is SearchResult){
                                        onDone(searchResult)
                                    } else if (searchResult is AutocompleteResult){
                                        findPlaces(searchResult, placesViewModel)
                                    }
                                }
                                true
                            } else {
                                false
                            }
                        }
                        .onFocusChanged { focusState ->
                            if (focusState.isFocused){
                                isTyping()
                            }
                        }
                )
            }

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

            if (autocompleteSuggestions.isNotEmpty()){
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
                                    if (objectResult is SearchResult){
                                        onDone(objectResult)
                                    } else if (objectResult is AutocompleteResult) {
                                        findPlaces(objectResult, placesViewModel)
                                    }
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

}