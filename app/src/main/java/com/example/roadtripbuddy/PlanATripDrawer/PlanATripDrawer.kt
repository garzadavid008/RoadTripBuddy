package com.example.roadtripbuddy.PlanATripDrawer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.roadtripbuddy.PlanATripViewModel
import com.example.roadtripbuddy.SearchManager
import java.util.Date

//Compose for the Search/Route page
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanATripDrawer(
    modifier: Modifier = Modifier,
    viewModel: PlanATripViewModel = viewModel(),
    visible: Boolean, // added parameter to control visibility
    onDismiss: () -> Unit,
    resolveAndSuggest: (String, (List<String>) -> Unit) -> Unit,//Function Parameter
    onRouteRequest: (PlanATripViewModel, Date) -> Unit,//Function Parameter
    clearMap: () -> Unit,//Function Parameter
    searchManager: SearchManager
) {
    val sheetState = rememberModalBottomSheetState()
    //AnimatedVisibility keeps state when the drawer is dismissed/not displayed
    AnimatedVisibility(visible = visible) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            modifier = modifier,
            scrimColor = Color.Transparent
        ) {
            PlanATripWaypoints(
                searchManager = searchManager,
                viewModel = viewModel,
                onRoute = {viewModel, departAT ->
                    clearMap()
                    onRouteRequest(viewModel, departAT)
                },
                performAutocomplete = resolveAndSuggest
            )
        }
    }
}

