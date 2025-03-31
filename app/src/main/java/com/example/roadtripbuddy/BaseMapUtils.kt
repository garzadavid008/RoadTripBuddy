package com.example.roadtripbuddy

import com.tomtom.sdk.datamanagement.navigationtile.NavigationTileStore
import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.map.display.TomTomMap
import com.tomtom.sdk.map.display.gesture.MapLongClickListener
import com.tomtom.sdk.map.display.image.ImageFactory
import com.tomtom.sdk.map.display.marker.MarkerOptions
import com.tomtom.sdk.navigation.TomTomNavigation
import com.tomtom.sdk.navigation.ui.NavigationFragment
import com.tomtom.sdk.search.model.result.SearchResult

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
    var startLocation: GeoPoint? = null
    var startLocationAddress: String? = ""

    //SEARCH FUNCTIONALITY(SearchManager methods)///////////////////////////////////////////////////


    // Optional parameter of a TripViewModel, should only be used when zooming in on a GeoPoint in order
    // to show the ETA
    fun performSearch(query: String, viewModel: SearchDrawerViewModel? = null) {
        searchManager.performSearch(
            query = query,
            viewModel = viewModel!!,
            clearMap = { tomTomMap?.clear() },
            tomTomMap = tomTomMap,
            planRouteAndGetETA = { options ->
                routeManager.planRouteAndGetETA(options)
            }

        )
    }

    fun searchResultGetter(query: String, callback: (SearchResult?) -> Unit){
        searchManager.searchResultGetter(
            query = query,
            callback = callback
        )
    }

    fun resolveAndSuggest(query: String, onResult: (List<String>) -> Unit = {}, objectResult: (Any?) -> Unit = {} ){
        searchManager.resolveAndSuggest(
            query = query,
            onResult = onResult,
            objectResult = objectResult
        )
    }

    fun updateStartLocation(location: GeoPoint?, onCallback: () -> Unit = {}){
        searchManager.updateStartLocation(
            location = location,
            onCallback = onCallback
        )
    }

    //ROUTING FUNCTIONALITY(RouteManager method(s))/////////////////////////////////////////////////

    //When the user requests a route this method is called, optional parameter for TripViewModel
    fun onRouteRequest(viewModel: SearchDrawerViewModel? = null){
        routeManager.onRouteRequest(
            tomTomMap = tomTomMap,
            viewModel = viewModel!!,
            searchManager = searchManager,
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

    fun removeMapListeners() {
        tomTomMap?.removeMapLongClickListener(mapLongClickListener)
    }

}
