package com.example.roadtripbuddy.pages

import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.roadtripbuddy.PlacesViewModel
import com.example.roadtripbuddy.PlacesViewModelFactory
import com.example.roadtripbuddy.R
import com.example.roadtripbuddy.SuggPlace
import com.example.roadtripbuddy.SuggestedLocation
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import android.Manifest
import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CardColors
import androidx.compose.material3.IconButton
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.rememberCoroutineScope
import com.tomtom.sdk.location.GeoPoint

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

// function to check if location permission is granted for error handling
private fun isPermissionGranted(context: Context,permission:String):Boolean
{
    return ContextCompat.checkSelfPermission(context,permission)== PackageManager.PERMISSION_GRANTED
}
data class Cords(val lat: Double, val long: Double)
@SuppressLint("MissingPermission")
suspend fun getCords(fusedLocationProviderClient: FusedLocationProviderClient, context: Context): Cords
{
    var latitdue : Double = 0.0
    var longitude: Double = 0.0
    // the code will run inside if user gives the location on start
    if (isPermissionGranted(context,Manifest.permission.ACCESS_FINE_LOCATION))
    {
        // using getLastLocation to retrieve the current location of the user
        fusedLocationProviderClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                Log.d("Fused","$location")
                if (location != null) {
                    latitdue = location.latitude
                    longitude = location.longitude
                }
            }
    }
  return Cords(latitdue,longitude)
}


// since we're not sure how long the number of suggested places is going to be, we can use LazyColumns
@OptIn(ExperimentalMaterial3Api::class)
@Composable
 fun Suggestions(
    navController: NavController,
    location: GeoPoint?,
    fusedLocationProviderClient: FusedLocationProviderClient,
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
    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = {navController.navigate("map")}) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF2ebcff),
                            modifier = Modifier.size(30.dp)
                        )
                    }
                },
                colors = TopAppBarColors(
                    containerColor          = Color(0xFFF5F5F5),      // light-gray background
                    scrolledContainerColor  = Color(0xFFE0E0E0),      // slightly darker when scrolled
                    navigationIconContentColor = Color(0xFF2EBCFF),   // cyan arrow tint
                    titleContentColor          = Color.Black,         // title text
                    actionIconContentColor     = Color(0xFF2EBCFF)
                ),
                title = {
                    Text("Suggested Locations")
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {

            CategoryFilteredList(
                foodList = myClass.foodList,
                entertainmentList = myClass.entertainment,
                gasList = myClass.gasAndService,
                onPlaceClick = { selectedPlace ->
                    // handle click

                }
            )
           // RightSidePanelDemo(myClass)^
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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryFilteredList(
    foodList: List<SuggPlace>,
    entertainmentList: List<SuggPlace>,
    gasList: List<SuggPlace>,
    onPlaceClick: (SuggPlace) -> Unit = {}
) {
    var selectedCategory by remember { mutableStateOf("food") }
    var fullPlaceList by remember { mutableStateOf(foodList.toList()) }

    // Category data holder using drawable resource icons
    data class Category(
        val key: String,
        val label: String,
        val iconRes: Int,
        val items: List<SuggPlace>
    )
    val categories = listOf(
        Category("food", "Food", R.drawable.lock, foodList),
        Category("entertainment", "Entertainment", R.drawable.games, entertainmentList),
        Category("gas", "Gas", R.drawable.car, gasList)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Nearby Places",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        TabRow(
            selectedTabIndex = categories.indexOfFirst { it.key == selectedCategory },
            containerColor = Color(0xFFdbf3fd),
            contentColor = Color.Black,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    Modifier.tabIndicatorOffset(tabPositions[categories.indexOfFirst { it.key == selectedCategory }]),
                    color = Color(0xFF2ebcff) // Set your desired indicator color here
                )
            }
        ) {
            categories.forEachIndexed { index, category ->
                Tab(
                    selected = selectedCategory == category.key,
                    onClick = {
                        selectedCategory = category.key
                        fullPlaceList = category.items.toList()
                    },
                    text = { Text(category.label) },
                    icon = {
                        Icon(
                            painter = painterResource(id = category.iconRes),
                            contentDescription = category.label,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(fullPlaceList) { place ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable { onPlaceClick(place) },
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardColors(
                        containerColor = Color(0xFFdbf3fd),
                        contentColor = Color.Black,
                        disabledContentColor = Color.White,
                        disabledContainerColor = Color.White
                        )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = place.name,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Rating: %.1f".format(place.rating),
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = place.address,
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "%.1f km away".format(place.distanceFromLocation),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}