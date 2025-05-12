package com.example.roadtripbuddy

import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import com.tomtom.sdk.location.DefaultLocationProviderFactory
import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.location.LocationProvider
import com.tomtom.sdk.location.OnLocationUpdateListener
import com.tomtom.sdk.map.display.camera.CameraOptions
import com.tomtom.sdk.map.display.location.LocationMarkerOptions
import com.tomtom.sdk.search.model.result.SearchResult
import com.tomtom.sdk.map.display.TomTomMap
import com.tomtom.sdk.map.display.image.ImageFactory
import com.tomtom.sdk.map.display.marker.Marker
import com.tomtom.sdk.map.display.marker.MarkerOptions


class LocationService(
    private val activity: AppCompatActivity,
) {
    private lateinit var locationProvider: LocationProvider
    private lateinit var onLocationUpdateListener: OnLocationUpdateListener

    private lateinit var map: NavigationMap
    private lateinit var isInitialCameraPositionSet: MutableState<Boolean>

    // Init flags for safe state
    private var isInitialized = false
    private var shouldEnableLocationAfterInit = false

    private var userLiveMarker: Marker? = null


    // Permissions handler
    private val locationPermissionRequest = activity.registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fine = permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarse = permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fine && coarse) {
            if (isInitialized) {
                enableUserLocation()
            } else {
                shouldEnableLocationAfterInit = true
            }
        }
    }

    fun mapLocationInitializer(
        mapInit: NavigationMap,
        isInitialCameraPositionSetInit: MutableState<Boolean>
    ) {
        map = mapInit
        isInitialCameraPositionSet = isInitialCameraPositionSetInit
        isInitialized = true

        if (shouldEnableLocationAfterInit) {
            enableUserLocation()
            shouldEnableLocationAfterInit = false
        }
    }

    fun enableUserLocation() {
        if (areLocationPermissionsGranted()) {
            initLocationProvider()
            showUserLocation()
        } else {
            requestLocationPermissions()
        }
    }

    private fun areLocationPermissionsGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            activity,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    activity,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    fun requestLocationPermissions() {
        locationPermissionRequest.launch(
            arrayOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    private fun initLocationProvider() {
        locationProvider = DefaultLocationProviderFactory.create(context = activity)
        locationProvider.enable()
    }

    private fun showUserLocation() {
        locationProvider.enable()

        onLocationUpdateListener = OnLocationUpdateListener { location ->
            val position = location.position

            map.updateStartLocation(position)
            if (!isInitialCameraPositionSet.value) {
                map.tomTomMap?.moveCamera(CameraOptions(position, zoom = 15.0))
                isInitialCameraPositionSet.value = true
                Log.d("Debug: Location", position.toString())
            }

        }

        locationProvider.addOnLocationUpdateListener(onLocationUpdateListener)
        map.tomTomMap?.setLocationProvider(locationProvider)

        val locationMarker = LocationMarkerOptions(type = LocationMarkerOptions.Type.Pointer)
        map.tomTomMap?.enableLocationMarker(locationMarker)
    }

    fun startLiveTracking() {
        locationProvider.removeOnLocationUpdateListener(onLocationUpdateListener)

        onLocationUpdateListener = OnLocationUpdateListener { location ->
            val position = location.position

            map.tomTomMap?.moveCamera(CameraOptions(position, zoom = 17.0))

        }

        locationProvider.addOnLocationUpdateListener(onLocationUpdateListener)
        map.tomTomMap?.setLocationProvider(locationProvider)
    }

    fun createRouteAndStart(viewModel: SearchDrawerViewModel) {
        if (!::map.isInitialized) {
            Log.e("LocationService", "Map not initialized. Cannot create route.")
            return
        }

        map.onRouteRequest(
            viewModel = viewModel,
            context = map.context
        )

        startLiveTracking()

        map.createRouteAndStart(viewModel) //
    }

    fun getLocationProvider(): LocationProvider {
        return locationProvider
    }

    fun getTomTomMap() = map.tomTomMap

    fun resetUserLiveMarker() {
        userLiveMarker = null
    }

    fun stopLiveTracking() {
        locationProvider.removeOnLocationUpdateListener(onLocationUpdateListener)
    }

}




