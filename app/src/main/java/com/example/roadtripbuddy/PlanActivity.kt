package com.example.roadtripbuddy

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.example.roadtripbuddy.SearchDrawer.SearchDrawer
import kotlinx.coroutines.delay

class PlanActivity : AppCompatActivity() {

    // We grab the usersLocation from the MainActivity
    private var usersLocationAddress: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        usersLocationAddress = intent.getStringExtra("start_location")
        setContent {
            PlanTripScreen(onBack = { finish() })
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun PlanTripScreen(onBack: () -> Unit) {
        val context = LocalContext.current
        val viewModel:TripViewModel by viewModels()
        var showBottomDrawer by remember { mutableStateOf(false) }
        var showStartLocationInput by remember { mutableStateOf(true) }
        val mapReadyState = remember { mutableStateOf(false) }
        val planMap = remember { PlanMap(
            context = context,
            activity = this@PlanActivity,
            mapReadyState = mapReadyState
        ) }

        Box(modifier = Modifier.fillMaxSize().background(Color.Transparent)) {
            Log.d("DEBUG", "mapReadyState.value before map init: ${mapReadyState.value}")
            planMap.PlanMapContent()

            if (mapReadyState.value && showStartLocationInput) {
                Log.d("DEBUG", "mapReadyState.value when passed: ${mapReadyState.value}")
                StartLocationInputBox(planMap) {
                    showStartLocationInput = false
                }
            }

            IconButton(
                onClick = {
                    onBack()
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

            FloatingActionButton(
                onClick = { showBottomDrawer = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(34.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search Navigation",
                    tint = Color(0xFF2ebcff),
                    modifier = Modifier.size(34.dp)
                )
            }
        }
    }

    @Composable
    fun StartLocationInputBox(
        planMap: PlanMap,
        onDismiss: () -> Unit
    ) {
        val focusManager = LocalFocusManager.current
        val defaultLocation = usersLocationAddress!! // Assumed non-null
        // Using TextFieldValue to capture both text and selection.
        var startLocationQuery by remember {
            mutableStateOf(TextFieldValue(defaultLocation, selection = TextRange(defaultLocation.length)))
        }
        var suggestions by rememberSaveable { mutableStateOf<List<String>>(emptyList()) }
        var isFieldFocused by remember { mutableStateOf(false) }

        // Full-screen container that clears focus on tap.
        Box(modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = { focusManager.clearFocus() })
            }
        ) {
            // Centered dialog-like content.
            Box(modifier = Modifier.align(Alignment.Center)) {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth(0.8f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        OutlinedTextField(
                            value = startLocationQuery,
                            onValueChange = { newValue ->
                                startLocationQuery = newValue
                            },
                            label = { Text("Start Location") },
                            singleLine = true,
                            maxLines = 1,
                            modifier = Modifier
                                .fillMaxWidth()
                                .onFocusChanged { focusState ->
                                    isFieldFocused = focusState.isFocused
                                    if (!focusState.isFocused) {
                                        suggestions = emptyList()
                                    }

                                }
                        )
                        // When focus becomes true, update the selection once.
                        LaunchedEffect(isFieldFocused) {
                            if (isFieldFocused) {
                                startLocationQuery = startLocationQuery.copy(
                                    selection = TextRange(startLocationQuery.text.length)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LaunchedEffect(startLocationQuery.text) {
                            // Trigger autocomplete only if the query is non empty
                            if (startLocationQuery.text.isNotEmpty()) {
                                delay(300)
                                planMap.resolveAndSuggest(query = startLocationQuery.text, onResult = { initSuggestions ->
                                    suggestions = initSuggestions.distinct()
                                })
                            } else {
                                suggestions = emptyList()
                            }
                        }
                        // Display suggestions only when the text field is focused
                        if (isFieldFocused && suggestions.isNotEmpty()) {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White)
                            ) {
                                items(suggestions) { suggestion ->
                                    Text(
                                        text = suggestion,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                // When selecting a suggestion, update the field and force the cursor to the end.
                                                startLocationQuery = TextFieldValue(
                                                    text = suggestion,
                                                    selection = TextRange(suggestion.length)
                                                )
                                                suggestions = emptyList()
                                                focusManager.clearFocus()
                                            }
                                            .padding(8.dp)
                                    )
                                }
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = {
                                    planMap.searchResultGetter(
                                        query = startLocationQuery.text,
                                        callback = { location ->
                                            if (location == null) {
                                                Toast.makeText(
                                                    this@PlanActivity,
                                                    "The location entered is not valid",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                return@searchResultGetter
                                            }
                                            planMap.updateStartLocation(location.place.coordinate)
                                            planMap.planATripCameraInit(location)
                                            onDismiss()
                                        }
                                    )
                                }
                            ) {
                                Text("Confirm")
                            }
                        }
                    }
                }
            }
        }
    }





}


