package com.example.roadtripbuddy

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
//import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.* // covers Arrangement, Box, Column, Row, Space, fillMaxSize,Height, padding, size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.* // covers DrawerValue, ExperimentalMaterial3Api, floatingactionbutton, icon, iconbutton, materialtheme, ect..
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
//import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.* // Covers derivedStateOf, getValue, mutableStateOf, remember, rememberCoroutineScope, setValue
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.roadtripbuddy.SearchDrawer.SearchDrawer
import com.example.roadtripbuddy.pages.LoginPage
import com.example.roadtripbuddy.pages.SignupPage
import com.example.roadtripbuddy.pages.Suggestions
//import com.example.roadtripbuddy.SearchDrawerViewModel
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
//import com.example.roadtripbuddy.AuthViewModel
//import com.example.roadtripbuddy.IAuthViewModel
import androidx.compose.ui.viewinterop.AndroidView
import com.example.roadtripbuddy.SearchDrawerViewModel
import com.example.roadtripbuddy.pages.LoginPage
import com.example.roadtripbuddy.pages.SignupPage
import kotlinx.coroutines.launch
import android.widget.FrameLayout
import androidx.compose.foundation.layout.fillMaxWidth
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import android.widget.Toast
import com.tomtom.sdk.datamanagement.navigationtile.NavigationTileStore
import com.tomtom.sdk.datamanagement.navigationtile.NdsLiveStoreAccess
import com.tomtom.sdk.map.display.TomTomMapConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers



class MainActivity : AppCompatActivity() {
    private val authViewModel: IAuthViewModel by viewModels<AuthViewModel>()
    private val locationService = LocationService(
        activity = this@MainActivity
    )
private lateinit var fusedLocationProviderClient: FusedLocationProviderClient // is this to get the current location of the user
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // calling firebase/firestore
        FirebaseApp.initializeApp(this)
        val firestore = FirebaseFirestore.getInstance()
        //creating the places api instance
        Places.initialize(applicationContext, getString(R.string.google_maps_key))
        val placesClient: PlacesClient = Places.createClient(this)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        locationService.requestLocationPermissions()

        // Only set content if not running in a test environment
        if (!isRunningTest()) {
            setContent {
                RoadTripBuddyApp()
            }
        }
    }

    // Helper function to detect test environment
    private fun isRunningTest(): Boolean {
        return try {
            Class.forName("androidx.test.espresso.Espresso")
            true
        } catch (e: ClassNotFoundException) {
            false
        }
    }

    @Composable
    private fun RoadTripBuddyApp() {
        val context = LocalContext.current
        val mapReadyState = remember { mutableStateOf(false) }
        val navigationMap = remember {
            NavigationMap(
                context = context,
                activity = this@MainActivity,
                locationService = locationService,
                mapReadyState = mapReadyState
            )
        }

        var suggestionPlace = remember { mutableStateOf<SuggPlace?>(null) }

        val navController = rememberNavController()
        val authState by authViewModel.authState.observeAsState()

        NavHost(
            navController = navController,
            startDestination = "map"
        ) {
            composable("map") { MapScreen(navController, navigationMap, suggestionPlace) }
            composable("about") { AboutScreen(navController) }
            composable("login") { LoginPage(navController, authViewModel) }
            composable("signup") { SignupPage(navController, authViewModel) }
            composable("suggestion") {
                Suggestions(
                    navController = navController,
                    location = navigationMap.searchManager.startLocation,
                    fusedLocationProviderClient = fusedLocationProviderClient,
                    onPlaceClick = {suggPlace ->
                        suggestionPlace.value = suggPlace
                    }
                ) }
        }
    }

    @SuppressLint("ContextCastToActivity", "UnrememberedMutableState", "MutableCollectionMutableState")
    @Composable
    fun MapScreen(
        navController: NavController,
        navigationMap: NavigationMap,
        suggestionPlace: MutableState<SuggPlace?>
    ) {
        val authState = authViewModel.authState.observeAsState()
        val searchDrawerVM: SearchDrawerViewModel by viewModels()
        val activity = LocalContext.current as MainActivity
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        val scope = rememberCoroutineScope()
        val gesturesStatus by remember {
            derivedStateOf { drawerState.isOpen }
        }
        var showBottomDrawer by remember { mutableStateOf(false) }
        var destinationSelected by remember { mutableStateOf(false) }

        val placesClient: PlacesClient = Places.createClient(activity)
        // view model for googles places
        val placesViewModel: PlacesViewModel = viewModel(factory = PlacesViewModelFactory(placesClient))
        // val placeList by viewModel.restaurants.collectAsState()
        val userViewModel: UserViewModel = viewModel()


        NavigationDrawer(
            drawerState = drawerState,
            userViewModel= userViewModel,
            gesturesStatus = gesturesStatus,
            authState = authState.value ?: AuthState.Unauthenticated,
            navController = navController,
            authViewModel = authViewModel,
            onItemClick = { item ->
                scope.launch {
                    drawerState.close()
                }
                when (item.id) {
                    "plan_a_trip" -> activity.startActivity(
                        Intent(activity, PlanActivity::class.java)
                            .putExtra("start_location", navigationMap.searchManager.startLocation)
                    )

                    "about" -> navController.navigate("about") {
                        popUpTo("map") {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }

                    "login" -> navController.navigate("login") {
                        popUpTo("map") {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }

                    "logout" -> authViewModel.signout() // sign out
                    "suggest" -> navController.navigate("suggestion"){
                        popUpTo("map") {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            }
        ) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Transparent)) {
                navigationMap.NavMapContent()

                IconButton(
                    onClick = {
                        scope.launch {
                            if (drawerState.isClosed) {
                                drawerState.open()
                            } else {
                                drawerState.close()
                            }
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Navigation Menu",
                        tint = Color(0xFF2ebcff),
                        modifier = Modifier.size(48.dp)
                    )
                }

                if (navigationMap.mapReadyState.value) {
                    SearchDrawer(
                        visible = showBottomDrawer,
                        placesViewModel = placesViewModel,
                        viewModel = searchDrawerVM,
                        onDismiss = { showBottomDrawer = false },
                        navMap = navigationMap,
                        searchManager = navigationMap.searchManager,
                        onStartTrip = { locationService.createRouteAndStart(searchDrawerVM) },
                        ifSuggestionPlace = suggestionPlace.value
                    )
                }

                if (destinationSelected) {
                    FloatingActionButton(
                        onClick = {
                            if (!activity.isFinishing && !activity.isDestroyed && navigationMap.mapReadyState.value) {
                                navigationMap.createRouteAndStart(searchDrawerVM)
                            } else {
                                Log.e("Navigation", "Map not ready yet.")
                            }                        },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp)
                    ) {
                        Text("Start Directions")
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun AboutScreen(navController: NavController) {
        val bgImage1 = painterResource(id = R.drawable.car_on_road_1740419)

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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "Main Menu", color = Color.Black)
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color(0xFF2ebcff)
                            )
                        }
                    }
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {
                // Background Image
                Image(
                    painter = bgImage1,
                    contentDescription = "Background Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Main Content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 80.dp, start = 16.dp, end = 16.dp), // Moves text up
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "RoadTripBuddy",
                        style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Center,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Planning for the Road",
                        style = TextStyle(fontStyle = FontStyle.Italic),
                        textAlign = TextAlign.Center,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "RoadTripBuddy is an application that helps users by providing information in regard to selected travel destinations, recommendations of places to visit from information provided, while providing in data regarding estimated travel time, fuel usage, possible locations for rest, and alternative routes should traffic be predicted to be heavy in certain time periods.",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Developer information:",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White
                    )
                    Text(
                        text = "Christopher Lopez\nDavid Garza\nJesus Aguilar\nLuis Vicencio",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = Color.White
                    )
                }
            }
        }
    }
}