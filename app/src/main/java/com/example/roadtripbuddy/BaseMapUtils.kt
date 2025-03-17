package com.example.roadtripbuddy

import PlacesViewModel
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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
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
import com.tomtom.sdk.search.model.result.AutocompleteResult
import com.tomtom.sdk.search.model.result.AutocompleteSegmentBrand
import com.tomtom.sdk.search.model.result.AutocompleteSegmentPoiCategory
import com.tomtom.sdk.search.model.result.SearchResult
import com.tomtom.sdk.search.online.OnlineSearch
import com.tomtom.sdk.vehicle.Vehicle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.time.Duration

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
    private var  fuzzySuggestionsPairs:  List<Pair<String, SearchResult?>> = emptyList()
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


    //User fuzzy search to simply convert a string address into a SearchResult object
    private fun searchResultGetter(query: String, callback: (SearchResult?) -> Unit){
        Log.d("BaseMap", query)
        val searchOptions = SearchOptions(query = query)

        searchApi.search(
            searchOptions,
            object : SearchCallback {
                override fun onSuccess(result: SearchResponse) {
                    callback(result.results.first()) //Returns a SearchObject as the callback
                }

                override fun onFailure(failure: SearchFailure) {
                    Toast.makeText(this@BaseMapUtils, failure.message, Toast.LENGTH_SHORT).show()
                    Log.d("BaseMapUtils", "search failed")
                }
            }
        )
    }

    // helper function so perform search can use Viewmodel and places client for the api calls
    fun createPlacesClientAndViewmodel(context: Context):PlacesViewModel{
        val placesClient = Places.createClient(context)
        return ViewModelProvider(context as ViewModelStoreOwner, PlacesViewModelFactory(placesClient))[PlacesViewModel::class.java]
    }
    //Function to either find, add marker, and zoom into an initial SearchResult, or to call the POI's Near function
    fun performSearch(query: String, eta: MutableState<String>) {
        if (query.isEmpty()) {
            Toast.makeText(this@BaseMapUtils, "No results found", Toast.LENGTH_SHORT).show()
            return
        }

        resolveAndSuggest(query, objectResult = { result ->
            if (result == null) {
                Toast.makeText(this@BaseMapUtils, "No search result found", Toast.LENGTH_SHORT)
                    .show()
                return@resolveAndSuggest
            }

            if (result is AutocompleteResult){ //If the object returned is a brand/poi category
                //POI's NEAR FUNCTION CALL GOES HERE
                // preparing the call
             //   val placesClient: PlacesClient = Places.createClient(LocalContext.current) // client resonsible for sending the request
               // val viewModel: PlacesViewModel = viewModel(factory = PlacesViewModelFactory(placesClient))  // stores the info from the API and prepares it for the UI
                //val placeList by viewModel.restaurants.collectAsState() // contains the list of nearby places and we display it on the screen

                val context = this@BaseMapUtils
                val viewModel = createPlacesClientAndViewmodel(context)
                // grabbing the users current position
                val location = tomTomMap?.currentLocation?.position
                if(location != null)
                {
                    // geo points of the users location
                    val lat = location.latitude.toDouble()
                    val long = location.longitude.toDouble()
                    // since its an api call, we need to do in a asyn structure
                    CoroutineScope(Dispatchers.IO).launch {
                        viewModel.getTextSearch(query,lat,long) // call the function
                        withContext(Dispatchers.Main)
                        {
                            val placeList = viewModel.restaurants.value
                            // loop through the list of places that were captured
                            placeList.forEach{ places ->
                                // add markers to the map
                                val geoPoint = GeoPoint(places.latAndLng.latitude,places.latAndLng.longitude)
                                // creating the mark
                                val mark = MarkerOptions(
                                    coordinate = geoPoint,
                                    pinImage = ImageFactory.fromResource(R.drawable.map_marker)
                                )
                                tomTomMap?.addMarker(mark)
                            }
                        }
                    }
                }


            }
            else { // If the object returned is an address
                val location = result as SearchResult// since objectResult is an Any object, we do this to say treat the result were talking about as a SearchResult
                val locationGeoPoint = location.place.coordinate

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
                    return@resolveAndSuggest
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
        })
    }

    //For the resolveAndSuggest function
    private fun fuzzySearchAutocomplete(
        query: String,
        onStringResult: (List<String>) -> Unit = {},
        onObjectResult: (List<SearchResult?>) -> Unit = {}
    ){
        val userLocation = tomTomMap?.currentLocation?.position ?: return
        if (query.isBlank()) {
            onStringResult(emptyList()) // Return empty list for empty queries
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
                    val stringSuggestions = result.results.mapNotNull { suggestion ->
                        suggestion.place.address?.freeformAddress
                    }

                    onObjectResult(result.results)
                    onStringResult(stringSuggestions)
                }

                override fun onFailure(failure: SearchFailure) {
                    Toast.makeText(this@BaseMapUtils, failure.message, Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    //A multi purpose function. From a string query it returns two things, a list of string address/brand/poi category suggestions
    // that are used to output under search bars, AND either an AutocompleteResult object (A brand/poi category) or a SearchResult object (an address)
    fun resolveAndSuggest(
        query: String,
        onResult: (List<String>) -> Unit = {},//An optional function parameter that returns a suggestion list of strings (this is for the search bar suggestions)
        objectResult: (Any?) -> Unit = {} //An optional function parameter that returns an object, either and AutocompleteResult or SearchResult
    ) {
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

        searchApi.autocompleteSearch(// first we use autocomplete search for brands and poi categories
            autocompleteOptions,
            object : AutocompleteCallback {
                override fun onSuccess(result: AutocompleteResponse) {
                    val autocompletePairs = result.results.mapNotNull { res -> // we make the results be a list of Pairs, each pair being made up of the string name, the other being the object
                        val display = res.segments.joinToString(" ") { segment ->
                            when (segment) {
                                is AutocompleteSegmentBrand -> segment.brand.name
                                is AutocompleteSegmentPoiCategory -> segment.poiCategory.name
                                else -> ""
                            }
                        }.trim()
                        if (display.isNotEmpty()) {
                            Pair(display, res)  //Pairs them
                        } else {
                            null
                        }
                    }


                    // Start with empty lists to store the fuzzy search results
                    var fuzzyStrings: List<String> = emptyList()
                    var fuzzyObjects: List<SearchResult?> = emptyList()

                    // Call fuzzySearchAutocomplete to perform the fuzzy search.
                    fuzzySearchAutocomplete(
                        query,
                        // Callback when string suggestions are ready.
                        onStringResult = { suggestions ->
                            // Update the local fuzzyStrings with the list provided
                            fuzzyStrings = suggestions

                            // If fuzzyObjects already contains data from the other callback, we combine the two lists into pairs
                            if (fuzzyObjects.isNotEmpty()) {
                                fuzzySuggestionsPairs = fuzzyStrings.zip(fuzzyObjects) { str, obj -> // pairs each element from fuzzyStrings with the corresponding element from fuzzyObjects
                                    Pair(str, obj)
                                }
                            }
                        },
                        // Callback when SearchResult objects are ready
                        onObjectResult = { results ->
                            // Update the local fuzzyObjects with the list provided.
                            fuzzyObjects = results

                            // If fuzzyStrings already contains data, we again combine the two lists into pairs
                            if (fuzzyStrings.isNotEmpty()) {
                                fuzzySuggestionsPairs = fuzzyStrings.zip(fuzzyObjects) { str, obj -> // pairing ensures that each string suggestion is associated with its corresponding SearchResult.
                                    Pair(str, obj)
                                }
                            }
                        }
                    )





                    val combinedResults = (fuzzySuggestionsPairs + autocompletePairs)//Combine the lists of pairs
                        .asSequence()
                        .sortedWith(
                            compareByDescending { pair ->

                                var score = 0f

                                if (pair.first.startsWith(query, ignoreCase = true)) score += 0.2f // Sort by strings

                                score
                            }
                        )
                        .take(5)
                        .toList()

                    objectResult(combinedResults.firstOrNull()?.second)// Return the first object on the list

                    onResult(combinedResults.map { it.first }) //Return a List of Strings for autocomplete suggestions
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
        etaState: MutableState<String>
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

            Log.d("WAYPOINS", waypointList.toString())

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

            routePlanner.planRoute(routePlanningOptions, callback)
        }
    }

    private suspend fun planRouteAndGetETA(options: RoutePlanningOptions): Duration =
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
