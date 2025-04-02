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
import com.tomtom.sdk.map.display.TomTomMap
import com.tomtom.sdk.map.display.camera.CameraOptions
import com.tomtom.sdk.map.display.location.LocationMarkerOptions
import com.tomtom.sdk.search.model.result.SearchResult

class LocationService( // Parameters to initialize the class ->
    private val activity: AppCompatActivity,
) {

    private lateinit var locationProvider: LocationProvider
    private lateinit var onLocationUpdateListener: OnLocationUpdateListener
    private var userLocationOn: Boolean = false
    private lateinit var map: BaseMapUtils
    private lateinit var isInitialCameraPositionSet: MutableState<Boolean>


    private val locationPermissionRequest = activity.registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] == true &&
            permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
            userLocationOn = true
        } else {
            //Toast.makeText(this, System.getString(R.string.location_permission_denied), Toast.LENGTH_SHORT).show()
            userLocationOn = false
        }
    }

    // Intakes a SearchResult, accesses its GeoPoint and moves the camera to said GeoPoint and adds a location marker

    // Initializes an instance of a map from the BaseMapUtils, and an optional parameter of boolean, the why is
    // further explained in the NavigationMap
    fun mapLocationInitializer(
        mapInit: NavigationMap,
        isInitialCameraPositionSetInit: MutableState<Boolean>? = null,
    ){
        map = mapInit
        isInitialCameraPositionSet = isInitialCameraPositionSetInit!!
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
        locationProvider.enable() // Requests location updates
    }

    private fun showUserLocation() {
        locationProvider.enable()

        onLocationUpdateListener =
            OnLocationUpdateListener { location ->
                if (!isInitialCameraPositionSet.value){ //On first composition of a session
                    map.tomTomMap?.moveCamera(CameraOptions(location.position, zoom = 15.0))// set the camera to the users location
                    isInitialCameraPositionSet.value = true // After the first time it never does it again on that session
                    map.updateStartLocation(location.position)// Call searchManagers updateStartLocation method
                    Log.d("Debug: Location", location.position.toString())
                }
                //locationProvider.removeOnLocationUpdateListener(onLocationUpdateListener)
            }

        locationProvider.addOnLocationUpdateListener(onLocationUpdateListener)
        map.tomTomMap?.setLocationProvider(locationProvider)

        val locationMarker = LocationMarkerOptions(type = LocationMarkerOptions.Type.Pointer)
        map.tomTomMap?.enableLocationMarker(locationMarker)
    }
}