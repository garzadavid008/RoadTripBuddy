package com.example.roadtripbuddy

import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.MutableState
import androidx.core.content.ContextCompat
import com.tomtom.sdk.location.DefaultLocationProviderFactory
import com.tomtom.sdk.location.LocationProvider
import com.tomtom.sdk.location.OnLocationUpdateListener
import com.tomtom.sdk.map.display.TomTomMap
import com.tomtom.sdk.map.display.camera.CameraOptions
import com.tomtom.sdk.map.display.image.ImageFactory
import com.tomtom.sdk.map.display.marker.MarkerOptions

class FLocationService(
    private val activity: AppCompatActivity,
) {
    private lateinit var locationProvider: LocationProvider
    private lateinit var onLocationUpdateListener: OnLocationUpdateListener
    private lateinit var tomTomMap: TomTomMap
    private lateinit var searchManager: SearchManager
    private lateinit var isInitialCameraPositionSet: MutableState<Boolean>

    private var isInitialized = false
    private var shouldEnableLocationAfterInit = false

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
        searchManagerInit: SearchManager,
        tomMapInit: TomTomMap?,
        isInitialCameraPositionSetInit: MutableState<Boolean>
    ) {
        searchManager = searchManagerInit
        tomTomMap = tomMapInit!!
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

            if (!isInitialCameraPositionSet.value) {
                tomTomMap.moveCamera(CameraOptions(position, zoom = 15.0))
                isInitialCameraPositionSet.value = true
            }

            // ✅ Just drop a new marker on each update
            try {
                val markerOptions = MarkerOptions(
                    coordinate = position,
                    pinImage = ImageFactory.fromResource(R.drawable.map_marker)
                )
                tomTomMap.addMarker(markerOptions)
            } catch (e: Exception) {
                Log.e("LiveLocation", "Failed to add marker: ${e.message}")
            }

            searchManager.updateStartLocation(position)
        }

        locationProvider.addOnLocationUpdateListener(onLocationUpdateListener)
        tomTomMap.setLocationProvider(locationProvider)
    }
}
