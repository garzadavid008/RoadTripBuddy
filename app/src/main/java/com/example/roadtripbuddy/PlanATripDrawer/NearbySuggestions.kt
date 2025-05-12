package com.example.roadtripbuddy.PlanATripDrawer

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.roadtripbuddy.PlacesViewModel
import com.example.roadtripbuddy.PlacesViewModelFactory
import com.example.roadtripbuddy.SuggPlace
import com.example.roadtripbuddy.SuggestedLocation
import com.example.roadtripbuddy.pages.CategoryFilteredList
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import com.tomtom.sdk.location.GeoPoint

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NearbySuggestions(
    location: GeoPoint?,
    onPlaceClick: (SuggPlace) -> Unit,
)
{

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // call the places view model
    // placeList will contain all suggested places
    val placesClient: PlacesClient = Places.createClient(LocalContext.current) // client resonsible for sending the request
    val viewModel: PlacesViewModel = viewModel(factory = PlacesViewModelFactory(placesClient))  // stores the info from the API and prepares it for the UI
    val includedTypes : MutableList<String> = mutableListOf()
    val GasincludedTypes : MutableList<String> = mutableListOf()
    val FunincludedTypes : MutableList<String> = mutableListOf()
    includedTypes.add("restaurant")

    GasincludedTypes.add("gas_station")
    GasincludedTypes.add("car_wash")
    FunincludedTypes.add("convention_center")
    FunincludedTypes.add("movie_theater")
    FunincludedTypes.add("park")

    val myClass = remember  { SuggestedLocation(viewModel) }
    LaunchedEffect(Unit) {
        if(location != null){
            myClass.setUpList(location.latitude, location.longitude, includedTypes, "food")
            myClass.setUpList(location.latitude, location.longitude, FunincludedTypes, "fun")
            myClass.setUpList(location.latitude, location.longitude, GasincludedTypes, "gas")
            Log.d("Chris","The size of list is ${myClass.foodList} ")
            Log.d("Chris","The size of viewModel ${viewModel.restaurants.value.size}")
            Log.d("Chris","Size of gas list ${myClass.gasAndService.size} and size of fun list ${myClass.entertainment}")
        }
    }

    Column() {
        CategoryFilteredList(
            foodList = myClass.foodList,
            entertainmentList = myClass.entertainment,
            gasList = myClass.gasAndService,
            onPlaceClick = { selectedPlace ->
                onPlaceClick(selectedPlace)
            }
        )
        // RightSidePanelDemo(myClass)^
    }

}