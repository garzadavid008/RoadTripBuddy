package com.example.roadtripbuddy


import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentContainerView
import kotlinx.coroutines.launch
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavController
import com.tomtom.sdk.map.display.MapOptions
import com.tomtom.sdk.map.display.ui.MapView
import android.content.Context
import androidx.activity.viewModels
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.roadtripbuddy.pages.LoginPage
import com.example.roadtripbuddy.pages.SignupPage
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore


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





    @Composable
    fun MapScreen(navController: NavController) {
        val authState = authViewModel.authState.observeAsState()

        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        val scope = rememberCoroutineScope()
        val gesturesStatus by remember {
            derivedStateOf { drawerState.isOpen }
        }

        ModalNavigationDrawer(
            drawerState = drawerState,
            gesturesEnabled = gesturesStatus,
            scrimColor = Color.Black.copy(alpha = 0.5f),
            drawerContent = {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(280.dp)
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(16.dp)
                ) {
                    DrawerHeader()
                    Spacer(modifier = Modifier.height(24.dp))
                    DrawerBody(
                        items = listOf(
                            MenuItems(
                                id = "plan_a_trip",
                                title = "Plan A Trip",
                                contentDescription = "Go to Plan A Trip"
                            ),
                            MenuItems(
                                id = "settings",
                                title = "Settings",
                                contentDescription = "Go to Settings"
                            ),
                            MenuItems(
                                id = "about",
                                title = "About",
                                contentDescription = "Go to About page"
                            ),* if (authState.value == AuthState.Unauthenicated)( // login
                                    arrayOf( MenuItems(
                                        id = "login",
                                        title = "Login",
                                        contentDescription = "Login page"
                                    )
                                    )) else if (authState.value == AuthState.Authenticated)( // singout
                                    arrayOf( MenuItems(
                                        id = "logout",
                                        title = "Logout",
                                        contentDescription = "Logout"
                                    )
                                    )) else emptyArray()
                        ),
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
                    )
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