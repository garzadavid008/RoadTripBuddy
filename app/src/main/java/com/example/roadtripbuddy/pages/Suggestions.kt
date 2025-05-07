package com.example.roadtripbuddy.pages

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
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
import com.example.roadtripbuddy.PlacesViewModel
import com.example.roadtripbuddy.PlacesViewModelFactory
import com.example.roadtripbuddy.R
import com.example.roadtripbuddy.SuggPlace
import com.example.roadtripbuddy.SuggestedLocation
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


// this will carry the list of LatLng objects
val listofLATandLong : MutableList<LatLng> = mutableListOf()

// this function will have a button that when clicked it will push geo codes into a list so we can use that for the map


data class Address ( var address: String)
data class PanelToggle (var isVis: Boolean)
//@Preview
@Composable
fun PlaceRow(
    name: String,
    rating: Double,
    address: String,
    latlng: LatLng,
    userAddress: Address = Address(""),
    onAdd: () -> Unit,
    distance: Double,
    onClick: () -> Unit,
    isSelected: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            // highlight background when selected
            .background(
                if (isSelected)
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                else
                    Color.Transparent
            )
            .clickable(onClick = onClick)
    ) {
        Divider(color = Color.LightGray, thickness = 1.dp)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilledTonalButton(
                onClick = {
                    userAddress.address = address
                    listofLATandLong.add(latlng)
                    onAdd()
                },
                colors = ButtonColors(
                    containerColor = Color(0xFFdbf3fd), contentColor = Color.Black,
                    disabledContainerColor = Color.White,
                    disabledContentColor = Color.White
                )
            ) {
                Text("+")
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(name, style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(2.dp))
                Text("${"%.1f".format(distance)} mi away", style = MaterialTheme.typography.bodySmall)
                Text("Rating: ${"%.1f".format(rating)}", style = MaterialTheme.typography.bodySmall)
                Text(address, style = MaterialTheme.typography.bodySmall, maxLines = 1)
            }
        }

        Divider(color = Color.LightGray, thickness = 1.dp)
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
        myClass.setUpList(26.243629, -98.245079, includedTypes, "food")
        myClass.setUpList(26.243629, -98.245079, FunincludedTypes, "fun")
        myClass.setUpList(26.243629, -98.245079, GasincludedTypes, "gas")
        Log.d("Chris","The size of list is ${myClass.foodList} ")
        Log.d("Chris","The size of viewModel ${viewModel.restaurants.value.size}")
        Log.d("Chris","Size of gas list ${myClass.gasAndService.size} and size of fun list ${myClass.entertainment}")
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
        Column(modifier = Modifier.padding(innerPadding)) {
            Text(
                "Places Near By",
                modifier = Modifier
                    .padding(vertical = 16.dp)
                    .align(Alignment.CenterHorizontally)
            )

            CategoryFilteredList(
                foodList = myClass.foodList,
                entertainmentList = myClass.entertainment,
                gasList = myClass.gasAndService,
                onPlaceClick = { selectedPlace ->
                    // handle click

                }
            )
            RightSidePanelDemo(myClass)
        }



    }
}


// refactor this later (make each question into a data class, make it so only one button is clicked at a time per question)
@Composable
fun RightSidePanelDemo(myClass:SuggestedLocation) {
    var isVisible by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxSize()) {
        Button(onClick = { isVisible = !isVisible; }, modifier = Modifier.align(Alignment.Center)) {
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
                    // sort the list by rating
                    Button(onClick = {}) {
                        Text("Sort List by Rating")
                    }
                    // sort it by diatnc
                    Button(onClick = {}) {
                        Text("Sort List by Distance ")
                    }
                }
            }
        }

    }

}


@Composable
fun PlaceListPage(
    placeList: List<SuggPlace>,
    placesViewModel: PlacesViewModel,
    userAddress: Address? = null,
    onPlaceClick: (SuggPlace) -> Unit = {},
    onBack: () -> Unit,
    onZoomOnPlace: (SuggPlace) -> Unit,
) {
    val scrollState = rememberLazyListState()
    val selectedPlace by placesViewModel.selectedPlace

    Box(
        modifier = Modifier
            .clickable { onBack() }
            .padding(24.dp)
    ) {
        Text("← Back", color = Color.Blue)
    }


    LazyColumn(
        state = scrollState,
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        items(placeList) { place ->
            // If place is the selected place, return true
            Log.d("PlacesListPage", place.distanceFromLocation.toString() )
            val isSel = place == selectedPlace
            PlaceRow(
                name = place.name,
                rating = place.rating,
                address = place.address,
                latlng = place.latAndLng,
                userAddress = userAddress ?: Address(""),
                onAdd = { onPlaceClick(place) },
                onClick = {
                    onZoomOnPlace(place)
                          },
                distance = place.distanceFromLocation,
                isSelected = isSel
            )
        }
    }
}

@Composable
fun CategoryFilteredList(
    foodList: List<SuggPlace>,
    entertainmentList: List<SuggPlace>,
    gasList: List<SuggPlace>,
    onPlaceClick: (SuggPlace) -> Unit = {}
) {
    var selectedCategory by remember { mutableStateOf("food") }
    var fullPlaceList by remember { mutableStateOf(foodList.toList()) }

    // Log.d("Chris","The size of the fullPlaceList ${fullPlaceList.size}")
    Column() {

        Row(

        ) {
            FilledTonalButton(onClick = {
                selectedCategory = "food"
                fullPlaceList = foodList.toList()
            }) {
                Image(
                    painter = painterResource(id = R.drawable.ham),
                    contentDescription = "Food"
                )
            }

            FilledTonalButton(onClick = {
                selectedCategory = "entertainment"
                fullPlaceList = entertainmentList.toList()
            }) {
                Image(
                    painter = painterResource(id = R.drawable.games),
                    contentDescription = "Games"
                )
            }

            FilledTonalButton(onClick = {
                selectedCategory = "gas"
                fullPlaceList = gasList.toList()
            }) {
                Image(
                    painter = painterResource(id = R.drawable.car2),
                    contentDescription = "Car"
                )
            }
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(fullPlaceList) { place ->
                PlaceRow(
                    name = place.name,
                    rating = place.rating,
                    address = place.address,
                    latlng = place.latAndLng,
                    onClick = { },
                    distance = place.distanceFromLocation,
                    onAdd = {onPlaceClick(place)},
                    isSelected = false

                )
            }

        }
    }
}