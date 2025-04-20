package com.example.roadtripbuddy

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.tomtom.sdk.map.display.MapOptions
import com.tomtom.sdk.map.display.ui.MapView
import com.tomtom.sdk.map.display.TomTomMap

// Custom Saver for MapView state
private fun mapViewStateSaver(context: Context, apiKey: String) = Saver<MapView, Bundle>(
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
fun TomTomMapComponent(
    modifier: Modifier = Modifier,
    apiKey: String,
    onMapReady: (TomTomMap) -> Unit, // Callback when map is ready
    onMapDispose: () -> Unit // Callback when composable is disposed
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var isMapInitialized by rememberSaveable { mutableStateOf(false) }

    val mapView = rememberSaveable(saver = mapViewStateSaver(context, apiKey)) {
        MapView(
            context = context,
            mapOptions = MapOptions(mapKey = apiKey)
        ).apply { onCreate(Bundle()) }
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
            Log.d("MAP", "MAP HAS BEEN DESTROYED")
            onMapDispose()
            isMapInitialized = false// Notify parent about disposal
        }
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier.fillMaxSize(),
        update = { view ->
            view.getMapAsync { map ->
                if (!isMapInitialized) {
                    view.markerBalloonViewAdapter = CustomBalloonViewAdapter(context)
                    onMapReady(map)
                }
                isMapInitialized = true
            }
        }
    )
}