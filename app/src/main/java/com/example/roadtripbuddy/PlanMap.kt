package com.example.roadtripbuddy

import android.content.Context
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import com.tomtom.sdk.map.display.camera.CameraOptions
import com.tomtom.sdk.map.display.image.ImageFactory
import com.tomtom.sdk.map.display.marker.MarkerOptions
import com.tomtom.sdk.map.display.route.Route
import com.tomtom.sdk.search.model.result.SearchResult

class PlanMap(
    private val context: Context,
    private val activity: PlanActivity,
    private val mapReadyState: MutableState<Boolean>
) : BaseMapUtils(){

    private lateinit var planRouteManager: PlanRouteManager

    @Composable
    fun PlanMapContent() {
        TomTomMapComponent(
            modifier = Modifier.fillMaxSize(),
            apiKey = apiKey,
            onMapReady = { map ->
                tomTomMap = map //Initializes the tomTomMap object
                searchManager = SearchManager(context = context, apiKey = apiKey)// Initializing an instance of the searchManager class
                planRouteManager = PlanRouteManager(context = context, apiKey = apiKey)// Initializing an instance of the routeManager class
                mapReadyState.value = true
            },
            onMapDispose = {
                tomTomMap = null
            }
        )

    }

    fun planATripCameraInit(searchResult: SearchResult?) {
        val location = searchResult?.place?.coordinate
        tomTomMap?.moveCamera(CameraOptions(location, zoom = 15.0))// set the camera to the users location
        val markerOptions = MarkerOptions(
            coordinate = location!!,
            pinImage = ImageFactory.fromResource(R.drawable.map_marker)
        )
        tomTomMap?.addMarker(markerOptions)
    }

    fun planOnRouteRequest(viewModel: PlanATripViewModel){
        planRouteManager.planOnRouteRequest(
            viewModel = viewModel,
            tomTomMap = tomTomMap,
        )
    }

    fun onRouteLegClick(route: Route,viewModel: PlanATripViewModel){
        planRouteManager.onRouteLegClick(
            routeClick = route,
            viewModel = viewModel,
            tomTomMap = tomTomMap
        )
    }

}