package com.example.roadtripbuddy

import android.app.Activity
import android.content.pm.PackageManager
import android.provider.Settings.Secure.getString
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import com.tomtom.sdk.location.DefaultLocationProviderFactory
import com.tomtom.sdk.location.LocationProvider
import com.tomtom.sdk.location.OnLocationUpdateListener
import com.tomtom.sdk.map.display.TomTomMap
import com.tomtom.sdk.map.display.camera.CameraOptions
import com.tomtom.sdk.map.display.location.LocationMarkerOptions

class LocationService( // Parameters to initialize the class ->
    private val activity: AppCompatActivity,
) {

    private lateinit var locationProvider: LocationProvider
    private lateinit var onLocationUpdateListener: OnLocationUpdateListener
    private var userLocationOn: Boolean = false
    private lateinit var searchManager: SearchManager
    private lateinit var tomTomMap: TomTomMap
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

    fun mapLocationInitializer(
        searchManagerInit: SearchManager,
        tomMapInit: TomTomMap?,
        isInitialCameraPositionSetInit: MutableState<Boolean>
    ){
        searchManager = searchManagerInit
        tomTomMap = tomMapInit!!
        isInitialCameraPositionSet = isInitialCameraPositionSetInit
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
                    tomTomMap.moveCamera(CameraOptions(location.position, zoom = 8.0))// set the camera to the users location
                    isInitialCameraPositionSet.value = true // After the first time it never does it again on that session
                }
                searchManager.updateStartLocation(location.position)
                //locationProvider.removeOnLocationUpdateListener(onLocationUpdateListener)
            }

        locationProvider.addOnLocationUpdateListener(onLocationUpdateListener)
        tomTomMap.setLocationProvider(locationProvider)

        val locationMarker = LocationMarkerOptions(type = LocationMarkerOptions.Type.Pointer)
        tomTomMap.enableLocationMarker(locationMarker)
    }
}