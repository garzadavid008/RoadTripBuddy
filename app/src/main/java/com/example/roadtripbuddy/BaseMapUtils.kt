package com.example.roadtripbuddy

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.tomtom.sdk.datamanagement.navigationtile.NavigationTileStore
import com.tomtom.sdk.location.DefaultLocationProviderFactory
import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.location.LocationProvider
import com.tomtom.sdk.location.OnLocationUpdateListener
import com.tomtom.sdk.location.Place
import com.tomtom.sdk.location.poi.StandardCategoryId.Companion.Locale
import com.tomtom.sdk.map.display.MapOptions
import com.tomtom.sdk.map.display.TomTomMap
import com.tomtom.sdk.map.display.camera.CameraOptions
import com.tomtom.sdk.map.display.gesture.MapLongClickListener
import com.tomtom.sdk.map.display.image.ImageFactory
import com.tomtom.sdk.map.display.location.LocationMarkerOptions
import com.tomtom.sdk.map.display.marker.Marker
import com.tomtom.sdk.map.display.marker.MarkerOptions
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
import com.tomtom.sdk.routing.options.ItineraryPoint
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
import com.tomtom.sdk.search.model.result.AutoCompleteResultType
import com.tomtom.sdk.search.model.result.AutocompleteResult
import com.tomtom.sdk.search.model.result.AutocompleteSegmentBrand
import com.tomtom.sdk.search.model.result.AutocompleteSegmentPlainText
import com.tomtom.sdk.search.model.result.AutocompleteSegmentPoiCategory
import com.tomtom.sdk.search.model.result.SearchResult
import com.tomtom.sdk.search.online.OnlineSearch
import com.tomtom.sdk.vehicle.Vehicle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.time.Duration
import kotlin.time.Duration.Companion.ZERO

open class BaseMapUtils : AppCompatActivity() {

    private val apiKey = BuildConfig.TOMTOM_API_KEY

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
    private var fuzzySuggestions: List<String> = emptyList()
    private var usersMarkerLocation: GeoPoint? = null
    private var waypointList = mutableListOf<ItineraryPoint>()
    private var lastDestination: GeoPoint? = null
    private var pendingClearMap: Boolean = false
    private var isMapInitialized: Boolean = false
    private var totalETA: String = ""

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

    // Composable function for displaying the Map
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
                isMapInitialized = false
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

                        if (pendingClearMap){
                            tomTomMap?.clear()
                            pendingClearMap = false
                        }

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

    //BASIC MAP FUNCTIONALITY//

    private fun initSearch(){
        searchApi = OnlineSearch.create(context = this@BaseMapUtils, apiKey = apiKey)
        Log.d("BaseMap","SearchApi initialized")
    }

    //Converts a string address into a SearchResult object
    private fun searchResultGetter(query: String, callback: (SearchResult?) -> Unit){
        Log.d("BaseMap", query)
        val searchOptions = SearchOptions(query = query)

        searchApi.search(
            searchOptions,
            object : SearchCallback {
                override fun onSuccess(result: SearchResponse) {
                    callback(result.results.first()) //Returns a SearchObject
                }

                override fun onFailure(failure: SearchFailure) {
                    Toast.makeText(this@BaseMapUtils, failure.message, Toast.LENGTH_SHORT).show()
                    Log.d("BaseMapUtils", "search failed")
                }
            },
        )
    }

    //Function to find, add marker, and zoom into the initial search location
    fun performSearch(query: String, eta: MutableState<String>) {
        if (query.isEmpty()) {
            Toast.makeText(this@BaseMapUtils, "No results found", Toast.LENGTH_SHORT).show()
            return
        }

        searchResultGetter(query) { result ->
            if (result == null) {
                Toast.makeText(this@BaseMapUtils, "No search result found", Toast.LENGTH_SHORT)
                    .show()
                return@searchResultGetter
            }

            val locationGeoPoint = result.place.coordinate

            clearMap()

            // Create marker options if the coordinate is available
            val markerOptions = MarkerOptions(
                coordinate = locationGeoPoint,
                pinImage = ImageFactory.fromResource(R.drawable.map_marker)
            )

            val userLocation = tomTomMap?.currentLocation?.position
            if (userLocation == null) {
                Toast.makeText(this@BaseMapUtils, "User location not available", Toast.LENGTH_SHORT)
                    .show()
                return@searchResultGetter
            }

            tomTomMap?.addMarker(markerOptions)
            tomTomMap?.moveCamera(CameraOptions(locationGeoPoint, zoom = 15.0))

            val itinerary = Itinerary(
                origin = ItineraryPoint(Place(userLocation)),
                destination = ItineraryPoint(Place(locationGeoPoint)),
                waypoints = waypointList  // or emptyList() if no waypoints are needed
            )
            val options = RoutePlanningOptions(
                itinerary = itinerary,
                guidanceOptions = GuidanceOptions(),
                vehicle = Vehicle.Car()
            )

            // Launch a coroutine to get the ETA asynchronously.
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val etaDuration = planRouteAndGetETA(options)
                    totalETA = etaDuration.toString()
                    eta.value = totalETA
                    Log.d("ETAAAAAAA", eta.value.toString())
                } catch (e: Exception) {
                    Toast.makeText(
                        this@BaseMapUtils,
                        "Route planning failed: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

        }
    }

    //For the Autocomplete function
    fun fuzzySearchAutocomplete(query: String, onResult: (List<String>) -> Unit){
        val userLocation = tomTomMap?.currentLocation?.position ?: return
        if (query.isBlank()) {
            onResult(emptyList()) // Return empty list for empty queries
            return
        }

        val searchOptions = SearchOptions(
            query = query,
            locale = Locale("en", "US"),
            limit = 5,
            geoBias = userLocation
        )
        searchApi.search(
            searchOptions,
            object : SearchCallback {
                override fun onSuccess(result: SearchResponse) {
                    val suggestions = result.results.mapNotNull { suggestion ->
                        suggestion.place.address?.freeformAddress
                    }

                    onResult(suggestions)
                }

                override fun onFailure(failure: SearchFailure) {
                    Toast.makeText(this@BaseMapUtils, failure.message, Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    //Performs the autocomplete for a searchBar, not perfect, will be cleaned up in the future
    fun performAutocomplete(query: String, onResult: (List<String>) -> Unit) {
        if (query.isBlank()) {
            onResult(emptyList()) // Return empty list for empty queries
            return
        }

        autocompleteOptions = AutocompleteOptions(
            query = query,
            position = tomTomMap?.currentLocation?.position,
            locale = Locale("en", "US"),
            limit = 5
        )

        searchApi.autocompleteSearch(
            autocompleteOptions,
            object : AutocompleteCallback {
                override fun onSuccess(result: AutocompleteResponse) {
                    val autocompleteSuggestions = result.results.mapNotNull { res ->
                        res.segments.joinToString(" ") { segment ->
                            when (segment) {
                                is AutocompleteSegmentBrand -> segment.brand.name
                                is AutocompleteSegmentPoiCategory -> segment.poiCategory.name
                                else -> null
                            } ?: ""
                        }.trim().takeIf { it.isNotEmpty() }
                    }

                    fuzzySearchAutocomplete(query){suggestions ->
                       fuzzySuggestions = suggestions
                    }

                    val combinedResults = (fuzzySuggestions + autocompleteSuggestions)
                        .asSequence()
                        .sortedWith(
                            compareByDescending { suggestion ->

                                var score = 0f

                                if (suggestion.contains(Regex("\\d+.*[A-Za-z]"))) score += 0.3f

                                if (suggestion.startsWith(query, ignoreCase = true)) score += 0.2f

                                score
                            }
                        )
                        .take(5)
                        .toList()

                    onResult(combinedResults)
                }

                override fun onFailure(failure: SearchFailure) {
                    Log.e("Autocomplete", "Error: ${failure.message}")
                    onResult(emptyList())
                }
            }
        )
    }

    private fun areLocationPermissionsGranted() = ContextCompat.checkSelfPermission(
        this,
        android.Manifest.permission.ACCESS_FINE_LOCATION,
    ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
        this,
        android.Manifest.permission.ACCESS_COARSE_LOCATION,
    ) == PackageManager.PERMISSION_GRANTED

    private val locationPermissionRequest =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions(),
        ) { permissions ->
            if (permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] == true &&
                permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] == true)
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
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
            ),
        )
    }

    private fun enableUserLocation() {
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

        totalETA = route.summary.travelTime.toString()

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

        //Adds a marker for each waypoint
        for (waypoint in waypointList){
            val markerOptions = MarkerOptions(
                coordinate = waypoint.place.coordinate,
                pinImage = ImageFactory.fromResource(R.drawable.map_marker)
            )

            tomTomMap?.addMarker(markerOptions)
        }

        tomTomMap?.addRoute(routeOptions)
        if (withZoom) {
            tomTomMap?.zoomToRoutes(ZOOM_TO_ROUTE_PADDING)
        }
    }

    companion object {
        private const val ZOOM_TO_ROUTE_PADDING = 100
    }

    //Calculates a route based on a list of addresses
    fun onRouteRequest(
        list: MutableList<String>,
        etaState: MutableState<String> // Add this parameter
    ) {
        routeLocationsConstructor(list) {
            if (lastDestination == null) {
                Log.e("BaseMapUtils", "Destination not found.")
                return@routeLocationsConstructor
            }

            Log.d("Map", lastDestination.toString())

            val userLocation =
                tomTomMap?.currentLocation?.position ?: return@routeLocationsConstructor

            val itinerary = Itinerary(
                origin = ItineraryPoint(Place(userLocation)),
                destination = ItineraryPoint(Place(lastDestination!!)),
                waypoints = waypointList
            )
            routePlanningOptions =
                RoutePlanningOptions(
                    itinerary = itinerary,
                    guidanceOptions = GuidanceOptions(),
                    vehicle = Vehicle.Car(),
                )

            // Define the callback inline to capture `etaState`
            val callback = object : RoutePlanningCallback {
                override fun onSuccess(result: RoutePlanningResponse) {
                    route = result.routes.first()
                    route?.let {
                        drawRoute(it) // Draw the route
                        // Update ETA state with travelTime
                        etaState.value = it.summary.travelTime.toString()
                    }
                }

                override fun onFailure(failure: RoutingFailure) {
                    Toast.makeText(this@BaseMapUtils, failure.message, Toast.LENGTH_SHORT).show()
                }

                override fun onRoutePlanned(route: Route) = Unit
            }

            routePlanner.planRoute(routePlanningOptions, callback) // Use the new callback
        }
    }

    suspend fun planRouteAndGetETA(options: RoutePlanningOptions): Duration =
        suspendCoroutine { cont ->
            routePlanner.planRoute(options, object : RoutePlanningCallback {
                override fun onSuccess(result: RoutePlanningResponse) {
                    val plannedRoute = result.routes.firstOrNull()
                    if (plannedRoute != null) {
                        cont.resume(plannedRoute.summary.travelTime)
                    } else {
                        cont.resumeWithException(Exception("No route found"))
                    }
                }
                override fun onFailure(failure: RoutingFailure) {
                    cont.resumeWithException(Exception(failure.message))
                }
                override fun onRoutePlanned(route: Route) = Unit
            })
        }

    fun clearMap(){
        if (tomTomMap != null){
            tomTomMap?.clear()
            pendingClearMap = false
        } else {
            pendingClearMap = true
        }

    }

    //At the moment all this does is add a marker, nothing else
    val mapLongClickListener =
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

    //WAYPOINT MANIPULATION

    //Adds a waypoint by taking a string address then converting it to a SearchResult Object, then to an ItineraryPoint
    private fun addWaypoint(query: String, onComplete: () -> Unit) {
        searchResultGetter(query) { newWaypoint ->
            if (newWaypoint != null) {
                waypointList.add(ItineraryPoint(Place(newWaypoint.place.coordinate)))
            } else {
                Log.e("BaseMapUtils", "Failed to get a waypoint for query: $query")
            }
            onComplete()//Callback because application needs the searchResultGetter function to finish before resuming
        }
    }

    //Constructs the users address list by initializing the waypointList and the lastDestination
    private fun routeLocationsConstructor(list: MutableList<String>, onComplete: () -> Unit) {
        // Reset the waypoint list.
        waypointList = mutableListOf()

        if (list.isEmpty()) {
            onComplete()
            return
        }

        // If there's only one location, simply initialize it as the lastDestination
        if (list.size == 1) {
            searchResultGetter(list.first()) { searchResult ->
                if (searchResult != null) {
                    lastDestination = searchResult.place.coordinate
                } else {
                    Log.e("BaseMapUtils", "Failed to set destination for: ${list.first()}")
                }
                onComplete()// Callback is used because the application needs to wait for the constructor to finish
            }
            return
        }

        // For all locations except the last, add them as waypoints.
        var completedCount = 0
        val totalWaypoints = list.size - 1
        list.forEachIndexed { index, location ->
            if (index < list.size - 1) {
                addWaypoint(location) {
                    completedCount++
                    if (completedCount == totalWaypoints) {
                        searchResultGetter(list.last()) { searchResult ->
                            if (searchResult != null) {
                                lastDestination = searchResult.place.coordinate
                            } else {
                                Log.e("BaseMapUtils", "Failed to set destination for: ${list.last()}")
                            }
                            onComplete()// Callback is used because the application needs to wait for the constructor to finish
                        }
                    }
                }
            }
        }
    }

    //MAP NAVIGATION//

}
