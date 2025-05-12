package com.example.roadtripbuddy

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import com.tomtom.sdk.common.UniqueId
import com.tomtom.sdk.location.poi.StandardCategoryId.Companion.Locale
import com.tomtom.sdk.map.display.camera.CameraOptions
import com.tomtom.sdk.map.display.image.ImageFactory
import com.tomtom.sdk.map.display.marker.Marker
import com.tomtom.sdk.map.display.marker.MarkerClickListener
import com.tomtom.sdk.map.display.marker.MarkerOptions
import com.tomtom.sdk.map.display.route.Route
import com.tomtom.sdk.search.Search
import com.tomtom.sdk.search.autocomplete.AutocompleteCallback
import com.tomtom.sdk.search.autocomplete.AutocompleteOptions
import com.tomtom.sdk.search.autocomplete.AutocompleteResponse
import com.tomtom.sdk.search.common.error.SearchFailure
import com.tomtom.sdk.search.model.result.AutocompleteResult
import com.tomtom.sdk.search.model.result.AutocompleteSegmentBrand
import com.tomtom.sdk.search.model.result.AutocompleteSegmentPoiCategory
import com.tomtom.sdk.search.model.result.SearchResult
import com.tomtom.sdk.search.online.OnlineSearch
import com.example.roadtripbuddy.LocationService
import java.util.Locale

class PlanMap(
    private val context: Context,
    private val activity: PlanActivity,
    private val mapReadyState: MutableState<Boolean>,
    private val locationService: LocationService
) : BaseMapUtils() {

    private lateinit var planRouteManager: PlanRouteManager

    @Composable
    fun PlanMapContent() {
        TomTomMapComponent(
            modifier = Modifier.fillMaxSize(),
            apiKey = apiKey,
            onMapReady = { map ->
                tomTomMap = map
                searchManager = SearchManager(context = context, apiKey = apiKey)
                routeManager = RouteManager(context = context, apiKey = apiKey)
                planRouteManager = PlanRouteManager(context = context, apiKey = apiKey)
                locationService.enableUserLocation()
                mapReadyState.value = true
            },
            onMapDispose = {
                tomTomMap = null
            }
        )
    }

    fun planATripCameraInit(searchResult: SearchResult?) {
        val location = searchResult?.place?.coordinate
        tomTomMap?.moveCamera(CameraOptions(location, zoom = 15.0))
        val markerOptions = MarkerOptions(
            coordinate = location!!,
            pinImage = ImageFactory.fromResource(R.drawable.map_marker_full)
        )
        tomTomMap?.addMarker(markerOptions)
    }

    fun planOnRouteRequest(viewModel: PlanATripViewModel) {
        planRouteManager.planOnRouteRequest(
            viewModel = viewModel,
            tomTomMap = tomTomMap,
            context = context
        )
    }

    fun onRouteLegClick(route: Route, viewModel: PlanATripViewModel) {
        planRouteManager.onRouteLegClick(
            routeClick = route,
            viewModel = viewModel,
            tomTomMap = tomTomMap
        )
    }

    fun brandsAndPOIOnly(
        query: String,
        location: SearchResult,
        onResult: (List<Pair<String, AutocompleteResult>>) -> Unit
    ) {
        if (query.isBlank()) {
            onResult(emptyList())
            return
        }

        val options = AutocompleteOptions(
            query = query,
            position = location.place.coordinate,
            locale = Locale("en", "US")
        )

        val searchApi: Search = OnlineSearch.create(context, apiKey)

        searchApi.autocompleteSearch(options, object : AutocompleteCallback {
            override fun onSuccess(result: AutocompleteResponse) {
                val autocompleteResult = result.results.mapNotNull { res ->
                    val display = res.segments.joinToString(" ") { segment ->
                        when (segment) {
                            is AutocompleteSegmentBrand -> segment.brand.name
                            is AutocompleteSegmentPoiCategory -> segment.poiCategory.name
                            else -> ""
                        }
                    }.trim()
                    if (display.isNotEmpty()) {
                        Pair(display, res)
                    } else {
                        null
                    }
                }
                onResult(autocompleteResult)
            }

            override fun onFailure(failure: SearchFailure) {
                Log.e("AutocompleteOnly", "autocomplete failed: ${failure.message}")
                onResult(emptyList())
            }
        })
    }
}