package com.example.roadtripbuddy

import android.content.Context
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier

//This is the map that appears in MainActivity. This map acts as a simple GPS navigation
open class NavigationMap(
    private val context: Context,
    private val activity: MainActivity,
    val mapReadyState: MutableState<Boolean>,
    private val locationService: LocationService// We intake the instance of the locationService class from the MainActivty
) : BaseMapUtils() { //Extending BaseMapUtils

    //INITIALIZING MAP COMPOSABLE///////////////////////////////////////////////////////////////////
    @Composable
    fun NavMapContent(){
        // This is to make sure the functionality that initially zooms into the users doesn't happen again
        // after first composition
        val isInitialCameraPositionSet = rememberSaveable { mutableStateOf(false) }

        TomTomMapComponent(
            modifier = Modifier.fillMaxSize(),
            apiKey = apiKey,
            onMapReady = { map ->
                tomTomMap = map //Initializes the tomTomMap object
                searchManager = SearchManager(// Initializing an instance of the searchManager class
                    context = context,
                    apiKey = apiKey
                )
                locationService.mapLocationInitializer(// Calling locationService method
                    mapInit = this,
                    isInitialCameraPositionSetInit = isInitialCameraPositionSet
                )
                locationService.enableUserLocation()
                setUpMapListeners()
                routeManager = RouteManager(context = context, apiKey = apiKey)// Initializing an instance of the routeManager class
                mapReadyState.value = true
            },
            onMapDispose = {
                tomTomMap = null
                removeMapListeners()
            }
        )
    }
    fun startTrip() {
        locationService.startLiveTracking()
    }



}