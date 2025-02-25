package com.example.roadtripbuddy

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.tomtom.sdk.datamanagement.navigationtile.NavigationTileStore
import com.tomtom.sdk.location.DefaultLocationProviderFactory
import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.location.LocationProvider
import com.tomtom.sdk.location.OnLocationUpdateListener
import com.tomtom.sdk.map.display.MapOptions
import com.tomtom.sdk.map.display.TomTomMap
import com.tomtom.sdk.map.display.camera.CameraOptions
import com.tomtom.sdk.map.display.gesture.MapLongClickListener
import com.tomtom.sdk.map.display.location.LocationMarkerOptions
import com.tomtom.sdk.map.display.route.Instruction
import com.tomtom.sdk.map.display.route.RouteOptions
import com.tomtom.sdk.map.display.ui.MapFragment
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
import com.tomtom.sdk.vehicle.Vehicle


class MainActivity : AppCompatActivity() {

    private val apiKey = BuildConfig.TOMTOM_API_KEY

    private lateinit var mapFragment: MapFragment
    private lateinit var tomTomMap: TomTomMap
    private lateinit var navigationTileStore: NavigationTileStore
    private lateinit var locationProvider: LocationProvider
    private lateinit var onLocationUpdateListener: OnLocationUpdateListener
    private lateinit var routePlanner: RoutePlanner
    private var route: Route? = null
    private lateinit var routePlanningOptions: RoutePlanningOptions
    private lateinit var tomTomNavigation: TomTomNavigation
    private lateinit var navigationFragment: NavigationFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.d("MainActivity", "TomTom API Key: $apiKey")

        initMap()

        initLocationProvider()

        initRouting()

        mapFragment.getMapAsync { map ->
            tomTomMap = map
        }

        //initNavigationTileStore()

        //initNavigation()
    }

    private fun initMap() {
        val mapOptions = MapOptions(mapKey = apiKey)
        mapFragment = MapFragment.newInstance(mapOptions)
        supportFragmentManager.beginTransaction()
            .replace(R.id.map_container, mapFragment)
            .commit()
        mapFragment.getMapAsync { map ->
            tomTomMap = map
            enableUserLocation()
            setUpMapListeners()
        }
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
            showUserLocation()
        }else {
            requestLocationPermissions()
        }
    }

    private fun initLocationProvider() {
        locationProvider = DefaultLocationProviderFactory.create(context = applicationContext)
        locationProvider.enable() //requests location updates
    }

    private fun showUserLocation() {
        locationProvider.enable()

        onLocationUpdateListener =
            OnLocationUpdateListener { location ->
                tomTomMap.moveCamera(CameraOptions(location.position, zoom = 8.0))
                locationProvider.removeOnLocationUpdateListener(onLocationUpdateListener)
            }
        locationProvider.addOnLocationUpdateListener(onLocationUpdateListener)
        tomTomMap.setLocationProvider(locationProvider)
        val locationMarker = LocationMarkerOptions(type = LocationMarkerOptions.Type.Pointer)
        tomTomMap.enableLocationMarker(locationMarker)
    }

    private fun initRouting () {
        routePlanner = OnlineRoutePlanner.create(context = applicationContext, apiKey = apiKey)
    }

    private val routePlanningCallback =
        object : RoutePlanningCallback {
            override fun onSuccess(result: RoutePlanningResponse) {
                route = result.routes.first()
                route?.let { drawRoute(it) }
            }

            override fun onFailure(failure: RoutingFailure) {
                Toast.makeText(this@MainActivity, failure.message, Toast.LENGTH_SHORT).show()
            }

            override fun onRoutePlanned(route: Route) = Unit
        }

    private fun drawRoute(
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
        tomTomMap.addRoute(routeOptions)
        if (withZoom) {
            tomTomMap.zoomToRoutes(ZOOM_TO_ROUTE_PADDING)
        }
    }
    companion object {
        private const val ZOOM_TO_ROUTE_PADDING = 100
    }

    private fun calculateRouteTo(destination: GeoPoint) {
        val userLocation =
            tomTomMap.currentLocation?.position ?: return
        val itinerary = Itinerary(origin = userLocation, destination = destination)
        routePlanningOptions =
            RoutePlanningOptions(
                itinerary = itinerary,
                guidanceOptions = GuidanceOptions(),
                vehicle = Vehicle.Car(),
            )
        routePlanner.planRoute(routePlanningOptions, routePlanningCallback)

    }

    private fun clearMap(){
        tomTomMap.clear()
    }

    private val mapLongClickListener =
        MapLongClickListener { geoPoint ->
            clearMap()
            calculateRouteTo(geoPoint)
            true
        }

    private fun setUpMapListeners() {
        tomTomMap.addMapLongClickListener(mapLongClickListener)
    }



}