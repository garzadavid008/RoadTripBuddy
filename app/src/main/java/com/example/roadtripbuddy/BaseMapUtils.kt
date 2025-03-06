package com.example.roadtripbuddy

import android.Manifest
import android.app.DownloadManager.Query
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.tomtom.sdk.datamanagement.navigationtile.NavigationTileStore
import com.tomtom.sdk.location.DefaultLocationProviderFactory
import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.location.LocationProvider
import com.tomtom.sdk.location.OnLocationUpdateListener
import com.tomtom.sdk.location.poi.StandardCategoryId.Companion.Locale
import com.tomtom.sdk.map.display.MapOptions
import com.tomtom.sdk.map.display.TomTomMap
import com.tomtom.sdk.map.display.camera.CameraOptions
import com.tomtom.sdk.map.display.gesture.MapLongClickListener
import com.tomtom.sdk.map.display.location.LocationMarkerOptions
import com.tomtom.sdk.map.display.route.Instruction
import com.tomtom.sdk.map.display.route.RouteOptions
import com.tomtom.sdk.map.display.ui.MapFragment
import com.tomtom.sdk.map.display.ui.MapView
import com.tomtom.sdk.navigation.TomTomNavigation
import com.tomtom.sdk.navigation.ui.NavigationFragment
import com.tomtom.sdk.routing.RoutePlanner
import com.tomtom.sdk.routing.RoutePlanningCallback
import com.tomtom.sdk.routing.RoutePlanningResponse
import com.tomtom.sdk.routing.RoutingFailure
import com.tomtom.sdk.routing.online.OnlineRoutePlanner
import com.tomtom.sdk.routing.options.Itinerary
import com.tomtom.sdk.routing.options.RoutePlanningOptions
import com.tomtom.sdk.routing.options.guidance.GuidanceOptions
import com.tomtom.sdk.routing.route.Route
import com.tomtom.sdk.search.Search
import com.tomtom.sdk.search.SearchCallback
import com.tomtom.sdk.search.SearchOptions
import com.tomtom.sdk.search.SearchResponse
import com.tomtom.sdk.search.autocomplete.AutocompleteCallback
import com.tomtom.sdk.search.autocomplete.AutocompleteOptions
import com.tomtom.sdk.search.autocomplete.AutocompleteResponse
import com.tomtom.sdk.search.common.error.SearchFailure
import com.tomtom.sdk.search.model.result.SearchResult
import com.tomtom.sdk.search.online.OnlineSearch
import com.tomtom.sdk.vehicle.Vehicle
import java.util.Locale
import kotlin.math.log

open class BaseMapUtils : AppCompatActivity() {

    private val apiKey = BuildConfig.TOMTOM_API_KEY

    private lateinit var mapFragment: MapFragment
    private var tomTomMap: TomTomMap? = null
    private lateinit var navigationTileStore: NavigationTileStore
    private lateinit var locationProvider: LocationProvider
    private lateinit var onLocationUpdateListener: OnLocationUpdateListener
    private lateinit var routePlanner: RoutePlanner
    private var route: Route? = null
    private lateinit var routePlanningOptions: RoutePlanningOptions
    private lateinit var tomTomNavigation: TomTomNavigation
    private lateinit var navigationFragment: NavigationFragment
    private lateinit var autocompleteOptions: AutocompleteOptions
    private lateinit var searchApi: Search




    private var isMapInitialized: Boolean = false



    // Custom Saver for MapView state
    private fun mapViewStateSaver(context: Context) = Saver<MapView, Bundle>(
        save = { mapView ->
            val bundle = Bundle()
            mapView.onSaveInstanceState(bundle)
            bundle
        },
        restore = { savedState ->
            MapView(
                context = context,
                mapOptions = MapOptions(mapKey = apiKey)
            ).apply {
                onCreate(savedState)
            }
        }
    )

    // Composable function for TomTom Map
    @Composable
    fun TomTomMap(modifier: Modifier = Modifier) {
        val context = LocalContext.current
        val lifecycleOwner = LocalLifecycleOwner.current

        val mapView = rememberSaveable(saver = mapViewStateSaver(context)) {
            MapView(
                context = context,
                mapOptions = MapOptions(mapKey = apiKey)
            ).apply {
                onCreate(Bundle())
                isMapInitialized = false
            }
        }

        DisposableEffect(lifecycleOwner) {
            val lifecycle = lifecycleOwner.lifecycle
            val observer = LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_START -> {
                        mapView.onStart()
                        Log.d("MapView", "ON START")
                    }
                    Lifecycle.Event.ON_RESUME -> {
                        mapView.onResume()
                        Log.d("MapView", "ON RESUME")
                    }
                    Lifecycle.Event.ON_PAUSE -> {
                        Log.d("MapView", "ON PAUSE")
                        mapView.onPause()
                    }
                    Lifecycle.Event.ON_STOP -> {
                        Log.d("MapView", "ON STOP")
                        mapView.onStop()
                    }
                    Lifecycle.Event.ON_DESTROY -> {
                        Log.d("MapView", "ON DESTROY")
                        mapView.onDestroy()
                    }
                    else -> {}
                }
            }

            lifecycle.addObserver(observer)

            onDispose {
                lifecycle.removeObserver(observer)
                tomTomMap = null
                if (lifecycle.currentState.isAtLeast(Lifecycle.State.DESTROYED)) {
                    mapView.onDestroy()
                }
            }
        }

        AndroidView(
            factory = { mapView },
            modifier = modifier.fillMaxSize(),
            update = { view ->
                view.getMapAsync { map ->
                    // Configure map when ready
                    if (!isMapInitialized){
                        tomTomMap = map
                        enableUserLocation()
                        setUpMapListeners()
                        initSearch()
                        initRouting()
                    }

                    isMapInitialized = true
                }
            }
        )
    }

    private fun initSearch(){
        searchApi = OnlineSearch.create(context = this@BaseMapUtils, apiKey = apiKey)
        Log.d("BaseMap","SearchApi initialized")
    }

    fun performSearch(query: String){
        Log.d("BaseMap", query)
        val userLocation = tomTomMap?.currentLocation?.position ?: return
        val searchOptions =
            SearchOptions(query = query, geoBias = userLocation)

        searchApi.search(
            searchOptions,
            object : SearchCallback {
                override fun onSuccess(result: SearchResponse) {
                    handleSearchResults(result.results)
                    Log.d("BaseMapUtils", "search succeeded")
                }

                override fun onFailure(failure: SearchFailure) {
                    Toast.makeText(this@BaseMapUtils, failure.message, Toast.LENGTH_SHORT).show()
                    Log.d("BaseMapUtils", "search failed")
                }
            },
        )
    }

    private fun handleSearchResults(results: List<SearchResult>){
        if (results.isEmpty()) {
            Toast.makeText(this@BaseMapUtils, "No results found", Toast.LENGTH_SHORT).show()
            return
        }
        val firstResult = results.first()
        clearMap()
        calculateRouteTo(firstResult.place.coordinate)
    }

    /*

    fun autocomplete(query: String){
        autocompleteOptions =
            AutocompleteOptions(
                query = query,
                locale = Locale("en", "US"),
                position = tomTomMap?.currentLocation?.position
            )

        searchApi.autocompleteSearch(
            autocompleteOptions,
            object : AutocompleteCallback {
                override fun onSuccess(result: AutocompleteResponse){

                }

                override fun onFailure(failure: SearchFailure) {
                    Toast.makeText(this@BaseMapUtils, failure.message, Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
     */

    private fun areLocationPermissionsGranted() = ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_FINE_LOCATION,
    ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_COARSE_LOCATION,
    ) == PackageManager.PERMISSION_GRANTED

    private val locationPermissionRequest =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions(),
        ) { permissions ->
            if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true &&
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true)
            {
                initLocationProvider()
                showUserLocation()
            } else {
                Toast.makeText(
                    this,
                    getString(R.string.location_permission_denied),
                    Toast.LENGTH_SHORT,
                ).show()
            }
        }

    private fun requestLocationPermissions() {
        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            ),
        )
    }

    fun enableUserLocation() {
        if (areLocationPermissionsGranted()){
            initLocationProvider()
            showUserLocation()
        }else {
            requestLocationPermissions()
        }
    }

    private fun initLocationProvider() {
        locationProvider = DefaultLocationProviderFactory.create(context = this@BaseMapUtils)
        locationProvider.enable() //requests location updates
    }

    private fun showUserLocation() {
        locationProvider.enable()

        onLocationUpdateListener =
            OnLocationUpdateListener { location ->
                tomTomMap?.moveCamera(CameraOptions(location.position, zoom = 8.0))
                locationProvider.removeOnLocationUpdateListener(onLocationUpdateListener)
            }
        locationProvider.addOnLocationUpdateListener(onLocationUpdateListener)
        tomTomMap?.setLocationProvider(locationProvider)
        val locationMarker = LocationMarkerOptions(type = LocationMarkerOptions.Type.Pointer)
        tomTomMap?.enableLocationMarker(locationMarker)
    }

    private fun initRouting () {
        routePlanner = OnlineRoutePlanner.create(context = this@BaseMapUtils, apiKey = apiKey)
    }

    private val routePlanningCallback =
        object : RoutePlanningCallback {
            override fun onSuccess(result: RoutePlanningResponse) {
                route = result.routes.first()
                route?.let { drawRoute(it) }
            }

            override fun onFailure(failure: RoutingFailure) {
                Toast.makeText(this@BaseMapUtils, failure.message, Toast.LENGTH_SHORT).show()
            }

            override fun onRoutePlanned(route: Route) = Unit
        }

    fun drawRoute(
        route: Route,
        color: Int = RouteOptions.DEFAULT_COLOR,
        withDepartureMarker: Boolean = true,
        withZoom: Boolean = true,
    ) {
        val instructions =
            route.legs
                .flatMap { routeLeg -> routeLeg.instructions }
                .map {
                    Instruction(
                        routeOffset = it.routeOffset,
                    )
                }
        val routeOptions =
            RouteOptions(
                geometry = route.geometry,
                destinationMarkerVisible = true,
                departureMarkerVisible = withDepartureMarker,
                instructions = instructions,
                routeOffset = route.routePoints.map { it.routeOffset },
                color = color,
                tag = route.id.toString(),
            )
        tomTomMap?.addRoute(routeOptions)
        if (withZoom) {
            tomTomMap?.zoomToRoutes(ZOOM_TO_ROUTE_PADDING)
        }
    }
    companion object {
        private const val ZOOM_TO_ROUTE_PADDING = 100
    }

    private fun calculateRouteTo(destination: GeoPoint) {
        val userLocation =
            tomTomMap?.currentLocation?.position ?: return
        val itinerary = Itinerary(origin = userLocation, destination = destination)
        routePlanningOptions =
            RoutePlanningOptions(
                itinerary = itinerary,
                guidanceOptions = GuidanceOptions(),
                vehicle = Vehicle.Car(),
            )
        routePlanner.planRoute(routePlanningOptions, routePlanningCallback)

    }

    fun clearMap(){
        tomTomMap!!.clear()
    }

    val mapLongClickListener =
        MapLongClickListener { geoPoint ->
            clearMap()
            calculateRouteTo(geoPoint)
            true
        }

    fun setUpMapListeners() {
        tomTomMap?.addMapLongClickListener(mapLongClickListener)
    }
}