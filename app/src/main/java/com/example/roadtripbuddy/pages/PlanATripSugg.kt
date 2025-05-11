package com.example.roadtripbuddy.pages

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.roadtripbuddy.PlacesViewModel
import com.example.roadtripbuddy.PlacesViewModelFactory
import com.example.roadtripbuddy.R
import com.example.roadtripbuddy.SuggPlace
import com.example.roadtripbuddy.SuggestedLocation
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanATripSuggest(lat:Double,long: Double)
{
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var cords by remember { mutableStateOf(Cords(0.0,0.0)) }

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
        scope.launch(Dispatchers.IO){
            myClass.setUpList(lat,long, includedTypes, "food")
            myClass.setUpList(lat,long, FunincludedTypes, "fun")
            myClass.setUpList(lat,long, GasincludedTypes, "gas")
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
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
            Text(text = "$cords")
            TripFilteredList(
                foodList = myClass.foodList,
                entertainmentList = myClass.entertainment,
                gasList = myClass.gasAndService,
                onPlaceClick = { selectedPlace ->
                }
            )
        }
    }
}
@Composable
fun TripFilteredList(
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