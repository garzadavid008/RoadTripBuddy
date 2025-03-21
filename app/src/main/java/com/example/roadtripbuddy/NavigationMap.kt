package com.example.roadtripbuddy

import android.content.Context
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier

//This is the map that appears in MainActivity. This map acts as a simple GPS navigation
open class NavigationMap(
    private val context: Context,
    private val activity: MainActivity,
    private val locationService: LocationService
) : BaseMapUtils() { //Extending BaseMapUtils

    //INITIALIZING MAP COMPOSABLE///////////////////////////////////////////////////////////////////
    @Composable
    fun BaseMapContent(){
        // This is to make sure the functionality that initially zooms into the users doesn't happen again
        // after first composition
        val isInitialCameraPositionSet = rememberSaveable { mutableStateOf(false) }

        TomTomMapComponent(
            modifier = Modifier.fillMaxSize(),
            apiKey = apiKey,
            onMapReady = { map ->
                tomTomMap = map //Initializes the tomTomMap object
                searchManager = SearchManager(context = context, apiKey = apiKey)// Initializing an instance of the searchManager class
                locationService.mapLocationInitializer(
                    searchManagerInit = searchManager,
                    tomMapInit = tomTomMap,
                    isInitialCameraPositionSetInit = isInitialCameraPositionSet
                )
                locationService.enableUserLocation()
                //After locationService is initialized we call the enableUserLocation method
                setUpMapListeners()
                routeManager = RouteManager(context = context, apiKey = apiKey)// Initializing an instance of the routeManager class
            },
            onMapDispose = {
                tomTomMap = null
            }
        )
    }



}