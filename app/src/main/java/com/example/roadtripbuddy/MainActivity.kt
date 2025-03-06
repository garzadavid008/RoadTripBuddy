package com.example.roadtripbuddy


import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
//import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore

class MainActivity : BaseMapUtils() {
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // calling firebase/firestore
        FirebaseApp.initializeApp(this)
         val firestore = FirebaseFirestore.getInstance()
//        val companion = Unit
//        companion object {
//
//            lateinit var firestore: FirebaseFirestore // This will hold our Firestore instance
//
//        }

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
            composable("login"){ LoginPage( navController,authViewModel) }
            composable("signup") {SignupPage(navController,authViewModel) }
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
                    "login" -> navController.navigate("login"){
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
            Box(modifier = Modifier.fillMaxSize().background(Color.Transparent)){
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
                    onClick = {showBottomDrawer = true},
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
                        onDismiss = {showBottomDrawer = false},
                        performSearch = {query -> activity.performSearch(query)})
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun AboutScreen(navController: NavController) {
        Scaffold(
            topBar = {
                TopAppBar(
                    colors = topAppBarColors(
                        containerColor = Color(0xFF6acfff),
                        titleContentColor = Color(0xFF2ebcff),
                    ),
                    title = { Text("About") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Default.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "HELLO",
                    style = MaterialTheme.typography.headlineMedium
                )
            }
        }
    }
}