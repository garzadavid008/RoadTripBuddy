package com.example.roadtripbuddy.PlanATripDrawer

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.composables.core.BottomSheet
import com.composables.core.ModalBottomSheet
import com.composables.core.Sheet
import com.composables.core.SheetDetent
import com.composables.core.SheetDetent.Companion.FullyExpanded
import com.composables.core.rememberBottomSheetState
import com.composables.core.rememberModalBottomSheetState
import com.example.roadtripbuddy.Autocomplete
import com.example.roadtripbuddy.PlanATripViewModel
import com.example.roadtripbuddy.PlanMap
import com.example.roadtripbuddy.RouteLegInfo
import com.example.roadtripbuddy.SearchManager
import com.example.roadtripbuddy.WaypointItem

//Compose for the Search/Route page
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanATripDrawer(
    modifier: Modifier = Modifier,
    onMapFocus: MutableState<Boolean>,
    viewModel: PlanATripViewModel = viewModel(),
    planMap: PlanMap,
    searchManager: SearchManager
) {
    Log.d("Debug", "PlanDrawer onMapFocus: $onMapFocus")

    val peek = SheetDetent(identifier = "peek") { containerHeight, sheetHeight ->
        containerHeight * 0.05f
    }

    val half = SheetDetent(identifier = "half") { containerHeight, sheetHeight ->
        containerHeight * 0.5f
    }

    val sheetState = rememberBottomSheetState(
        initialDetent = half,
        detents = listOf(peek, half, FullyExpanded)
    )

    var ifAutocomplete by remember { mutableStateOf(false) }
    var routeLegInfo by remember { mutableStateOf(false) }
    val routeFlag  = rememberSaveable { mutableStateOf(false) }
    var planWaypointPair by remember { mutableStateOf<Pair<Int, WaypointItem?>?>(null) } // For the autocomplete composable page,
    val selectedRoutePair by viewModel.selectedRoutePair

    if (sheetState.currentDetent != peek){
        onMapFocus.value = false
    }

    // If selectedRoutePair sees changes
    LaunchedEffect(selectedRoutePair) {
        if(selectedRoutePair != null){
            // show route info page if a route has been clicked on
            routeLegInfo = true
        }
        // if we detect a change in the selectedRoutePair and its null, click off the routeLegInfo page
        else {
            routeLegInfo = false
        }
    }


    BottomSheet(
        state = sheetState,
        modifier = Modifier
            .shadow(4.dp, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .background(Color.White)
            .widthIn(max = 640.dp)
            .fillMaxWidth()
            .imePadding()
    ) {
            if (ifAutocomplete){
                Autocomplete(
                    resolveAndSuggest = {query, onResult ->
                        planMap.resolveAndSuggest(query = query, onResult = onResult)
                    },
                    address = planWaypointPair?.second?.searchResult?.place?.address?.freeformAddress ?: "",
                    onDone = { searchResult ->
                        // If the planWaypointPair.second (AKA the searchResult) is null, were adding so we call addWaypoint
                        if (planWaypointPair?.second == null){
                            viewModel.addPlanWaypoint(searchResult)
                        } else{ // else were updating a waypoints location
                            viewModel.updateSearchResult(
                                index = planWaypointPair!!.first,
                                newSearchResult = searchResult
                            )
                        }
                        ifAutocomplete = false
                        routeFlag.value = false // make the route flag false to let the RouteEditPage that the route function needs to be called
                    },
                    onBack = {
                        ifAutocomplete = false
                    }
                )
            } else if (routeLegInfo) {
                sheetState.currentDetent = half
                RouteLegInfo(
                    selectedRoutePair = selectedRoutePair,
                    onBack = {
                        routeLegInfo = false
                        planMap.onRouteLegClick(
                            route = selectedRoutePair?.first!!,
                            viewModel = viewModel
                        )
                    }
                )
            } else {
                PlanATripWaypoints(
                    searchManager = searchManager,
                    viewModel = viewModel,
                    onRoute = {viewModel->
                        planMap.clearMap()
                        planMap.planOnRouteRequest(viewModel)
                    },
                    resolveAndSuggest = {query, onResult ->
                        planMap.resolveAndSuggest(query = query, onResult = onResult)
                    },
                    onPlanWaypointEdit = { pair ->
                        //when user clicks on a waypoint they want to edit, RouteEditPage returns a
                        // pair of the index and the waypoint at said index
                        planWaypointPair = pair // Store it in the waypoint
                        ifAutocomplete = true
                    },
                    onPlanWaypointAdd = {
                        planWaypointPair = Pair(0, null)// Make the waypoint pair a null to send to Autocomplete
                        ifAutocomplete = true
                    }
                )
            }
    }
}
