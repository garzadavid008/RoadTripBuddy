package com.example.roadtripbuddy.TripSelect

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.roadtripbuddy.data.Trip
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripSelect(
    onBack: () -> Unit,
    onOpenTrip: (Long) -> Unit,
    viewModel: TripsViewModel = hiltViewModel()
) {

    val trips by viewModel.trips.collectAsState(initial = emptyList())
    var showDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        containerColor = Color.White,

        /* ─── Top bar ────────────────────────────────────── */
        topBar = {
            TopAppBar(
                title = { Text("Plan A Trip")},
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF2ebcff),
                            modifier = Modifier.size(30.dp)
                        )
                    }
                },
                actions = {
                    Button(
                        onClick = { showDialog = true},
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2ebcff),
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("New Trip")
                    }
                },
                colors = TopAppBarColors(
                    containerColor          = Color(0xFFF5F5F5),      // light-gray background
                    scrolledContainerColor  = Color(0xFFE0E0E0),      // slightly darker when scrolled
                    navigationIconContentColor = Color(0xFF2EBCFF),   // cyan arrow tint
                    titleContentColor          = Color.Black,         // title text
                    actionIconContentColor     = Color(0xFF2EBCFF)
                )
                
            )
        },

        /* ─── Content area ───────────────────────────────── */
        content = { inner ->
            if (trips.isEmpty()){
                Box(
                    modifier = Modifier
                        .padding(inner)
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center            // centers both axes
                ){
                    Text(
                        text = "No Trips",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                    )
                }
            }else {
                LazyColumn(
                    modifier = Modifier
                        .padding(inner)
                        .fillMaxSize()
                ) {
                    items(trips) { trip ->
                        Row(modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TripRow(
                                trip = trip,
                                onTripClick = { onOpenTrip(trip.id) },
                                onTripDelete = { viewModel.deleteTrip(trip.id) },
                                onTripRename = {newName ->
                                    viewModel.renameTrip(trip.id, newName)
                                }
                            )

                        }
                    }
                }
            }

            if (showDialog) {
                var nameError by remember { mutableStateOf(false) }
                NewTripDialog(
                    onDone = { name ->
                        if (trips.any { it.name.equals(name, ignoreCase = true) }) {
                            nameError = true
                        } else {
                            coroutineScope.launch {
                                val newId = viewModel.addTrip(name)
                                showDialog = false
                                nameError = false
                                onOpenTrip(newId)
                            }
                        }
                    },
                    onDismiss = {
                        showDialog = false
                        nameError = false
                    },
                    nameError = nameError // You'll need to pass this into the dialog and show an error UI
                )
            }
        }
    )
}




