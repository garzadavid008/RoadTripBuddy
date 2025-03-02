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
import androidx.activity.viewModels
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
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentContainerView
import kotlinx.coroutines.launch
// Add these imports
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.roadtripbuddy.pages.LoginPage
import com.example.roadtripbuddy.pages.SignupPage
import com.example.roadtripbuddy.AuthState

class MainActivity : BaseMapUtils() {
    val authViewModel: AuthViewModel by viewModels()
    // cannot use this in this main funciton
    //val authState = authViewModel.authState.observeAsState()
    val authState = authViewModel.authState
    // creating a separate list for conditional items to show on the navbar
    val conditionalMenuItems = mutableListOf<MenuItems>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // creating the user state/ view model
        // creatintg the view model to pass into the login and sign up functions


        setContent {
            val navController = rememberNavController()

            NavHost(
                navController = navController,
                startDestination = "map"
            ) {
                composable("map") {
                    MapScreen(
                        baseMapUtils = this@MainActivity,
                        navController = navController
                    )
                }
                composable("about") {
                    AboutScreen(navController = navController)
                }

                composable("login")
                {
                    LoginPage( navController,authViewModel) // this links to the login page
                }
                composable("signup")
                {
                    SignupPage(navController,authViewModel) // this links to the sign up page
                }

            }
            initRouting()
        }
    }

    @Composable
    fun MapScreen(baseMapUtils: BaseMapUtils, navController: NavController) {
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        val scope = rememberCoroutineScope()

        val context = LocalContext.current
        val fragmentManager = (context as FragmentActivity).supportFragmentManager

        // Use remember to preserve the containerId across recompositions
        val containerId = remember { View.generateViewId() }
        var isMapInitialized by rememberSaveable { mutableStateOf(false) }


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
                            ),
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
                            }
                        }
                    )
                }
            }
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Use AndroidView with the preserved containerId
                AndroidView(
                    factory = { ctx ->
                        FragmentContainerView(ctx).apply {
                            id = containerId
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            baseMapUtils.initMap(containerId)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )


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