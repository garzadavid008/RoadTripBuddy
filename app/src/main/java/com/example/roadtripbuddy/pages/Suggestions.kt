package com.example.roadtripbuddy.pages

import PlacesViewModel
import SuggPlace
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.roadtripbuddy.PlacesViewModelFactory
import com.example.roadtripbuddy.R
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient


// this will carry the list of LatLng objects
val listofLATandLong : MutableList<LatLng> = mutableListOf()

// this function will have a button that when clicked it will push geo codes into a list so we can use that for the map


data class Address ( var address: String)
data class PanelToggle (var isVis: Boolean)
//@Preview
@Composable
fun  placeCard(name:String="",rating: Double = 0.0, address:String = "", latlng:LatLng= LatLng(0.0,0.0),userAddress: Address= Address(""),onClick:()->Unit )
{
    var isVisible by remember { mutableStateOf(true) }
    // card for suggested place

AnimatedVisibility(visible = isVisible) {
    OutlinedCard(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        border = BorderStroke(1.dp, Color.Black),
        modifier = Modifier
            .wrapContentSize()
            .size(width = 275.dp, height = 140.dp).padding(10.dp).fillMaxWidth()
        ,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            FilledTonalButton(onClick = {
                //toggle.isVis= false
                //push the address into the list for the Searchmanager performSearch can use it
                userAddress.address = address
                listofLATandLong.add(latlng)
                // make the button invis
                isVisible = false
                // trigger the action
                onClick()
            }
            ) {
                Text("+")
            }
           // Image(painter = painterResource(id = R.drawable.filler), contentDescription = "Filler image", modifier = Modifier.size(50.dp))
            Column {
                Text("Place $name", modifier = Modifier.padding(10.dp))
                Text("Rating $rating",modifier = Modifier.padding(10.dp))
                Text("Address $address",modifier = Modifier.padding(10.dp))

                }
            }
        }
    }



}



// since we're not sure how long the number of suggested places is going to be, we can use LazyColumns
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Suggestions(navController: NavController)
{
    // call the places view model
    // placeList will contain all suggested places
    val placesClient: PlacesClient = Places.createClient(LocalContext.current) // client resonsible for sending the request
    val viewModel: PlacesViewModel = viewModel(factory = PlacesViewModelFactory(placesClient))  // stores the info from the API and prepares it for the UI
    val placeList by viewModel.restaurants.collectAsState() // contains the list of nearby places and we display it on the screen
    val userList12 :MutableList<String> = mutableListOf()
    val wantedAddress : Address = Address("")
    wantedAddress.address= "test test"
    // this function will run when page is loaded
    LaunchedEffect(Unit) {
        //viewModel.getNearbyRestaurants(26.243629, -98.245079) // Example location
        viewModel.getTextSearch("whataburger",26.243629, -98.245079)
    }
    Scaffold(
        topBar = {
            TopAppBar(
                colors = topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Text("Suggested Locations")
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(innerPadding)  // Apply Scaffold's inner padding
                .padding(16.dp),        // Add any additional padding you need
            contentPadding = PaddingValues(16.dp)
        ) {
            // list of suggestions here
            item {
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .wrapContentHeight()
                        .padding(vertical = 25.dp),
                    horizontalArrangement = Arrangement.Center, // center the data
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Places Near By",
                       // style = MaterialTheme.typography.bodySmall
                    )
                    RightSidePanelDemo(placeList,wantedAddress)
                }
            }
//            // this will generate the list of places
//            items(placeList)
//            { place ->
//               // placeCard(place.name,place.rating,place.address,place.latAndLng)
//            }
        }

    }

}

@Preview
@Composable
fun RightSidePanelDemo(placeList: List<SuggPlace> = emptyList(), userAddress: Address= Address("")) {
    var isVisible by remember { mutableStateOf(false) }
    //var toggle2: PanelToggle by remember { mutableStateOf(false) }
//    var toggle : PanelToggle = PanelToggle(false)
//    toggle.isVis= isVisible

    Box(modifier = Modifier.fillMaxSize()) {
        Button(onClick = { isVisible = !isVisible }, modifier = Modifier.align(Alignment.Center)) {
            Text("Toggle Panel")
        }

        AnimatedVisibility(
            visible = isVisible,
            enter = slideInHorizontally { it }, // Slide in from right
            exit = slideOutHorizontally { it }, // Slide out to right
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            Box(
                modifier = Modifier
                    .width(250.dp)
                    .fillMaxHeight()
                    .background(Color.LightGray)
                    .padding(16.dp)
            ) {
                Column {
                    Text("Near By Locations", fontWeight = FontWeight.Bold)

                    Column {
                        // this will generate the list of places
                        placeList.forEach { place ->
                          //  placeCard(place.name, place.rating, place.address, place.latAndLng,userAddress)
                            //isVisible= toggle.isVis
                        }

                      //  repeat(10) { Text("Item $it", modifier = Modifier.padding(8.dp)) }
                        Button(onClick = { isVisible = false }) {
                            Text("Close")
                        }
                    }
                }
            }
        }

    }
    Log.i("Chris"," The current String is ${userAddress.address}")
}

@Composable
fun PlaceListPage(
    placeList: List<SuggPlace>,
    userAddress: Address? = null,
    onPlaceClick: (SuggPlace) -> Unit = {}
) {
    val scrollState = rememberLazyListState()
    LazyColumn(
        state = scrollState,
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        items(placeList) { place ->
            placeCard(
                name = place.name,
                rating = place.rating,
                address = place.address,
                latlng = place.latAndLng,
                userAddress = userAddress ?: Address(""),
                onClick = { onPlaceClick(place) }
            )
        }
    }
}

