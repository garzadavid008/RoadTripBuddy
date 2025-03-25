package com.example.roadtripbuddy

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings.System.getString
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.roadtripbuddy.SearchDrawer.SearchDrawerViewModel
import com.tomtom.sdk.datamanagement.navigationtile.NavigationTileStore
import com.tomtom.sdk.location.DefaultLocationProviderFactory
import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.location.LocationProvider
import com.tomtom.sdk.location.OnLocationUpdateListener
import com.tomtom.sdk.location.Place
import com.tomtom.sdk.map.display.MapOptions
import com.tomtom.sdk.map.display.TomTomMap
import com.tomtom.sdk.map.display.camera.CameraOptions
import com.tomtom.sdk.map.display.gesture.MapLongClickListener
import com.tomtom.sdk.map.display.image.ImageFactory
import com.tomtom.sdk.map.display.location.LocationMarkerOptions
import com.tomtom.sdk.map.display.marker.MarkerOptions
import com.tomtom.sdk.map.display.route.Instruction
import com.tomtom.sdk.map.display.route.RouteOptions
import com.tomtom.sdk.map.display.ui.MapView
import com.tomtom.sdk.navigation.TomTomNavigation
import com.tomtom.sdk.navigation.ui.NavigationFragment

//All the methods in this class are directly used in MainActivity
open class BaseMapUtils{

    val apiKey = BuildConfig.TOMTOM_API_KEY
    lateinit var searchManager: SearchManager
    lateinit var routeManager: RouteManager
    var tomTomMap: TomTomMap? = null
    private lateinit var navigationTileStore: NavigationTileStore
    private lateinit var tomTomNavigation: TomTomNavigation
    private lateinit var navigationFragment: NavigationFragment
    private var usersMarkerLocation: GeoPoint? = null
    private var pendingClearMap: Boolean = false

    //SEARCH FUNCTIONALITY(SearchManager methods)///////////////////////////////////////////////////

    fun performSearch(query: String, viewModel: SearchDrawerViewModel) {
        searchManager.performSearch(
            query = query,
            viewModel = viewModel,
            clearMap = { tomTomMap?.clear() },
            tomTomMap = tomTomMap,
            planRouteAndGetETA = { options ->
                routeManager.planRouteAndGetETA(options)
            }

        )
    }

    fun resolveAndSuggest(query: String, onResult: (List<String>) -> Unit = {}, objectResult: (Any?) -> Unit = {} ){
        searchManager.resolveAndSuggest(
            query = query,
            tomTomMap = tomTomMap,
            onResult = onResult,
            objectResult = objectResult
        )
    }

    //ROUTING FUNCTIONALITY(RouteManager method(s))/////////////////////////////////////////////////

    //When the user requests a route this method is called
    fun onRouteRequest(viewModel: SearchDrawerViewModel){
        routeManager.onRouteRequest(
            tomTomMap = tomTomMap,
            viewModel = viewModel,
            searchManager = searchManager
        )
    }

    //BASIC TOM_TOM_MAP FUNCTIONALITY///////////////////////////////////////////////////////////////
    fun clearMap(){
        if (tomTomMap != null){
            tomTomMap?.clear()
            pendingClearMap = false
        } else {
            pendingClearMap = true
        }

    }

    //At the moment all this does is add a marker, nothing else
    private val mapLongClickListener =
        MapLongClickListener { geoPoint ->
            //clearMap()
            val markerOptions =  MarkerOptions(
                coordinate = geoPoint,
                pinImage = ImageFactory.fromResource(R.drawable.map_marker),
            )
            tomTomMap?.addMarker(markerOptions)
            usersMarkerLocation = geoPoint
            true
        }

    fun setUpMapListeners() {
        tomTomMap?.addMapLongClickListener(mapLongClickListener)
    }
}
