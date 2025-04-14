package com.example.roadtripbuddy.PlanATripDrawer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.roadtripbuddy.Autocomplete
import com.example.roadtripbuddy.PlanATripViewModel
import com.example.roadtripbuddy.SearchManager
import com.example.roadtripbuddy.WaypointItem
import com.tomtom.sdk.search.model.result.SearchResult
import java.util.Date

//Compose for the Search/Route page
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanATripDrawer(
    modifier: Modifier = Modifier,
    viewModel: PlanATripViewModel = viewModel(),
    visible: Boolean, // added parameter to control visibility
    onDismiss: () -> Unit,
    resolveAndSuggest: (String, (List<Pair<String, Any?>>) -> Unit) -> Unit,//Function Parameter
    onRouteRequest: (PlanATripViewModel) -> Unit,//Function Parameter
    clearMap: () -> Unit,//Function Parameter
    searchManager: SearchManager
) {
    val sheetState = rememberModalBottomSheetState()
    var ifAutocomplete by remember { mutableStateOf(false) }
    val routeFlag  = rememberSaveable { mutableStateOf(false) }
    var planWaypointPair by remember { mutableStateOf<Pair<Int, WaypointItem?>?>(null) } // For the autocomplete composable page,
    //AnimatedVisibility keeps state when the drawer is dismissed/not displayed
    AnimatedVisibility(visible = visible) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            modifier = modifier,
            scrimColor = Color.Transparent
        ) {
            if (!ifAutocomplete){
                PlanATripWaypoints(
                    searchManager = searchManager,
                    viewModel = viewModel,
                    onRoute = {viewModel->
                        clearMap()
                        onRouteRequest(viewModel)
                    },
                    resolveAndSuggest = resolveAndSuggest,
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
            } else {
                Autocomplete(
                    resolveAndSuggest = resolveAndSuggest,
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
            }

        }
    }
}

