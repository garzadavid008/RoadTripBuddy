package com.example.roadtripbuddy


import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.Image // Renders images in JetpackCompose
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Brush // Need to be able to use gradiant backgrounds
import androidx.compose.ui.layout.ContentScale // For the images to scale properly
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle // Specifies if text is either normal or italic
import androidx.compose.ui.text.TextStyle // defines styles for text, font size, colour, height,ect
import androidx.compose.ui.res.painterResource // For loading drawable images
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavController
import androidx.activity.viewModels
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.LocalContext
import com.example.roadtripbuddy.pages.LoginPage
import com.example.roadtripbuddy.pages.SignupPage
import androidx.compose.ui.text.style.TextAlign


class MainActivity : BaseMapUtils() {
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RoadTripBuddyApp()
        }

    }

    @Composable
    private fun RoadTripBuddyApp() {
        val navController = rememberNavController()
        NavHost(
            navController = navController,
            startDestination = "map"
        ) {
            composable("map") { MapScreen(navController) }
            composable("about") { AboutScreen(navController) }
            composable("login") { LoginPage(navController, authViewModel) }
            composable("signup") { SignupPage(navController, authViewModel) }
        }
    }

    @SuppressLint("ContextCastToActivity")
    @Composable
    fun MapScreen(navController: NavController) {
        val authState = authViewModel.authState.observeAsState()
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
                TomTomMap(modifier = Modifier.fillMaxSize())
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

                if (showBottomDrawer) {
                    SearchDrawer(
                        onDismiss = { showBottomDrawer = false },
                        performSearch = { query -> activity.performSearch(query) })
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