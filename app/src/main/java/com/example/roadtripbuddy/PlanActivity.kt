package com.example.roadtripbuddy

import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.roadtripbuddy.PlanATripDrawer.PlanATripDrawer
import com.example.roadtripbuddy.TripSelect.TripSelect
import com.example.roadtripbuddy.TripSelect.TripsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PlanActivity : AppCompatActivity() {

    // We grab the usersLocation from the MainActivity
    private var usersLocationAddress: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        usersLocationAddress = intent.getStringExtra("start_location")
        setContent {
            PlanTripScreen(onBack = {
                finish() })
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun PlanTripScreen(onBack: () -> Unit) {
        val context = LocalContext.current
        val planVM:PlanATripViewModel by viewModels()
        val tripVM: TripsViewModel by viewModels()
        var showBottomDrawer by remember { mutableStateOf(false) }
        var mapFocus = rememberSaveable { mutableStateOf(false) }
        var tripSelectScreen by remember { mutableStateOf(true) }
        val mapReadyState = remember { mutableStateOf(false) }
        var currentTripId by remember { mutableLongStateOf(0) }

        val planMap = remember { PlanMap(
            context = context,
            activity = this@PlanActivity,
            mapReadyState = mapReadyState
        ) }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
        ){
            Log.d("DEBUG", "mapReadyState.value before map init: ${mapReadyState.value}")
            planMap.PlanMapContent()

            if (tripSelectScreen) {
                TripSelect(
                    onBack = {onBack()},
                    // onOpenTrip returns a tripId to load it onto the PlanATripViewModel
                    onOpenTrip = { tripId ->
                        tripVM.getTrip(tripId)?.let { planVM.loadTrip(it) }
                        // we then equal the tripId to the currentTripId
                        currentTripId = tripId
                        tripSelectScreen = false
                        showBottomDrawer = true
                    },
                    viewModel = tripVM
                )
            }
            else if (mapReadyState.value && showBottomDrawer){
                PlanATripDrawer(
                    onMapFocus = mapFocus,
                    viewModel = planVM,
                    planMap = planMap,
                    // onBack returns a bool for saving
                    onBack = { saveBool ->
                        // if user said yes we call saveTrip
                        if (saveBool){
                            // take a snapshot since we clear the waypoints below
                            val snapshot = planVM.planWaypoints.value.toList()
                            val initialDeparture = planVM.initialDeparture.value.time
                            tripVM.saveTrip(currentTripId, snapshot, initialDeparture)
                        }
                        planVM.clearPlanWaypoints()
                        planMap.clearMap()
                        showBottomDrawer = false
                        tripSelectScreen = true
                    }
                )
            }
            Log.d("Debug", "PlanActivity onMapFocus: $mapFocus")

        }
    }


}


