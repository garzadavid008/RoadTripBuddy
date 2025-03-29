package com.example.roadtripbuddy


import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.provider.Settings.System
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.roadtripbuddy.SearchDrawer.SearchDrawer
import com.example.roadtripbuddy.TripViewModel
import com.example.roadtripbuddy.pages.LoginPage
import com.example.roadtripbuddy.pages.SignupPage
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val authViewModel: AuthViewModel by viewModels()

    private val locationService = LocationService(
        activity = this@MainActivity
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        locationService.requestLocationPermissions()

        setContent {
            RoadTripBuddyApp()
        }
    }

    @Composable
    private fun RoadTripBuddyApp() {
        val context = LocalContext.current
        val mapReadyState = remember { mutableStateOf(false) }

        val navigationMap = remember { NavigationMap(
            context = context,
            activity = this@MainActivity,
            locationService = locationService,
            mapReadyState = mapReadyState
        )
        }

        val navController = rememberNavController()

        NavHost(
            navController = navController,
            startDestination = "map"
        ) {
            composable("map") { MapScreen(navController, navigationMap) }
            composable("about") { AboutScreen(navController) }
            composable("login") { LoginPage(navController, authViewModel) }
            composable("signup") { SignupPage(navController, authViewModel) }
        }
    }

    @SuppressLint("ContextCastToActivity", "UnrememberedMutableState",
        "MutableCollectionMutableState"
    )
    @Composable
    fun MapScreen(navController: NavController, navigationMap: NavigationMap) {
        val authState = authViewModel.authState.observeAsState()
        val searchDrawerVM: TripViewModel by viewModels()
        val activity = LocalContext.current as MainActivity
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        val scope = rememberCoroutineScope()
        val gesturesStatus by remember {
            derivedStateOf { drawerState.isOpen }
        }
        var showBottomDrawer by remember { mutableStateOf(false) }

        NavigationDrawer(
            drawerState = drawerState,
            gesturesStatus = gesturesStatus,
            authState = authState.value ?: AuthState.Unauthenticated,
            navController = navController,
            authViewModel = authViewModel,
            onItemClick = { item ->
                scope.launch {
                    drawerState.close()
                }
                when (item.id) {
                    "plan_a_trip" -> activity.startActivity(Intent(this@MainActivity, PlanActivity::class.java)
                        .putExtra("start_location", navigationMap.searchManager.startLocationAddress))

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
                }
            }
        ) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Transparent)) {
                navigationMap.NavMapContent()

                // Once BaseMapContent is initialized, we can initialize the Intent
                // We do this to pass the startLocation (AKA the users location) from the navigationMap.searchManager class instance
                // and in order to not have to ask for the users location again.

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

                FloatingActionButton(
                    onClick = { showBottomDrawer = true },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search Navigation",
                        tint = Color(0xFF2ebcff),
                        modifier = Modifier.size(34.dp)
                    )
                }
                    if (navigationMap.mapReadyState.value) {
                        SearchDrawer(
                            visible = showBottomDrawer,
                            viewModel = searchDrawerVM,
                            onDismiss = {showBottomDrawer = false},
                            performSearch = {query, eta -> navigationMap.performSearch(query, eta)},
                            resolveAndSuggest = {query, onResult ->
                                navigationMap.resolveAndSuggest(query, onResult)
                            },
                            onRouteRequest = {viewModel ->
                                navigationMap.onRouteRequest(
                                    viewModel = viewModel,
                                )},
                            clearMap = {navigationMap.clearMap()},
                            searchManager = navigationMap.searchManager
                        )
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
                    colors = topAppBarColors(
                        containerColor = Color.Transparent, // Makes the top bar invisible
                        titleContentColor = Color.Transparent, // Hides the title text
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
                                contentDescription = "Back"
                            )
                        }
                    },
                    modifier = Modifier.background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.White, Color.Gray.copy(alpha = 0.3f))
                        )
                    )
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