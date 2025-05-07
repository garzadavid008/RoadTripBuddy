package com.example.roadtripbuddy

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import com.tomtom.sdk.common.UniqueId
import com.tomtom.sdk.datamanagement.navigationtile.NavigationTileStore
import com.tomtom.sdk.datamanagement.navigationtile.NavigationTileStoreConfiguration
import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.map.display.TomTomMap
import com.tomtom.sdk.map.display.camera.CameraOptions
import com.tomtom.sdk.map.display.gesture.MapLongClickListener
import com.tomtom.sdk.map.display.image.ImageFactory
import com.tomtom.sdk.map.display.marker.Marker
import com.tomtom.sdk.map.display.marker.MarkerClickListener
import com.tomtom.sdk.map.display.marker.MarkerOptions
import com.tomtom.sdk.navigation.TomTomNavigation
import com.tomtom.sdk.navigation.ui.NavigationFragment
import com.tomtom.sdk.search.model.result.AutocompleteResult
import com.tomtom.sdk.search.model.result.SearchResult
import kotlin.time.Duration.Companion.seconds

//All the methods in this class are directly used in MainActivity
open class BaseMapUtils{

    val apiKey = BuildConfig.TOMTOM_API_KEY
    lateinit var searchManager: SearchManager
    lateinit var routeManager: RouteManager
    var tomTomMap: TomTomMap? = null

    private var usersMarkerLocation: GeoPoint? = null
    private var pendingClearMap: Boolean = false

    //Nearby Markers variables
    var nearbyMarkerClickListener: MarkerClickListener? = null
    private val nearbySelectedMap = mutableMapOf<UniqueId, SuggPlace>()
    private val allMarkers = mutableListOf<Marker>()

    //SEARCH FUNCTIONALITY(SearchManager methods)///////////////////////////////////////////////////


    // Optional parameter of a TripViewModel, should only be used when zooming in on a GeoPoint in order
    // to show the ETA
    fun performSearch(query: String, viewModel: SearchDrawerViewModel? = null,placesViewModel: PlacesViewModel? = null ,context: Context? = null) {
        searchManager.performSearch(
            query = query,
            viewModel = viewModel!!,
            clearMap = { tomTomMap?.clear() },
            tomTomMap = tomTomMap,
            planRouteAndGetETA = { options ->
                routeManager.planRouteAndGetETA(options)
            }

        )
    }

    fun findPlaces(
        result: AutocompleteResult,
        placesViewModel: PlacesViewModel?,
        location: GeoPoint? = searchManager.startLocation
    ){
        searchManager.findPlaces(
            result = result,
            location = location,
            tomTomMap = tomTomMap,
            placesViewModel = placesViewModel
        )
    }

    fun searchResultGetter(query: String, callback: (SearchResult?) -> Unit){
        searchManager.searchResultGetter(
            query = query,
            callback = callback
        )
    }

    fun resolveAndSuggest(query: String, onResult: (List<Pair<String, Any?>>) -> Unit = {}, objectResult: (Any?) -> Unit = {} ){
        searchManager.resolveAndSuggest(
            query = query,
            onResult = onResult,
            objectResult = objectResult
        )
    }

    fun updateStartLocation(location: GeoPoint?, onCallback: () -> Unit = {}){
        searchManager.updateStartLocation(
            location = location,
            onCallback = onCallback
        )
    }

    //ROUTING FUNCTIONALITY(RouteManager method(s))/////////////////////////////////////////////////

    //When the user requests a route this method is called, optional parameter for TripViewModel

    fun onRouteRequest(viewModel: SearchDrawerViewModel? = null, context: Context?){
        if (context != null) {
            routeManager.onRouteRequest(
                tomTomMap = tomTomMap,
                viewModel = viewModel!!,
                searchManager = searchManager,
                context = context
            )
        }
    }

    //BASIC TOM_TOM_MAP FUNCTIONALITY///////////////////////////////////////////////////////////////
    fun clearMap(){
        if (tomTomMap != null){
            tomTomMap?.clear()
            pendingClearMap = false
        } else {
            pendingClearMap = true
        }

    }

    //At the moment all this does is add a marker, nothing else
    private val mapLongClickListener =
        MapLongClickListener { geoPoint ->
            val markerOptions =  MarkerOptions(
                coordinate = geoPoint,
                pinImage = ImageFactory.fromResource(R.drawable.map_marker_full),
            )
            tomTomMap?.addMarker(markerOptions)
            usersMarkerLocation = geoPoint
            true
        }

    /*
    fun setUpMapListeners() {
        tomTomMap?.addMapLongClickListener(mapLongClickListener)
    }

    fun removeMapListeners() {
        tomTomMap?.removeMapLongClickListener(mapLongClickListener)
    }

     */

    fun defaultCameraPosition(startLocation: GeoPoint?){
        var defaultCameraPosition = CameraOptions()
        if (startLocation != null){
            defaultCameraPosition = CameraOptions(
                position = startLocation,
                zoom = 3.0
            )
        }else {
            defaultCameraPosition = CameraOptions(
                position = GeoPoint(38.00, -97.00),
                zoom = 4.0
            )
        }
        tomTomMap?.moveCamera(defaultCameraPosition)
    }

    fun removeMarkerClickListener(listener: MarkerClickListener){
        tomTomMap?.removeMarkerClickListener(listener)
    }


    //NEARBY MARKER FUNCTIONALITY///////////////////////////////////////////////////////////////////

    fun showNearbyMarkers(locations: List<SuggPlace>, placesViewModel: PlacesViewModel) {
        val selectedLocation by placesViewModel.selectedPlace

        nearbySelectedMap.clear()
        allMarkers.clear()

        if(locations.isNotEmpty()){
            locations.forEach { place ->
                val marker = if(selectedLocation == place){
                    tomTomMap?.addMarker(
                        MarkerOptions(
                            coordinate = GeoPoint(place.latAndLng.latitude, place.latAndLng.longitude),
                            tag = "nearby",
                            pinImage = ImageFactory.fromResource(R.drawable.map_marker_full)
                        )
                    )
                } else {
                    tomTomMap?.addMarker(
                        MarkerOptions(
                            coordinate = GeoPoint(place.latAndLng.latitude, place.latAndLng.longitude),
                            tag = "nearby",
                            pinImage   = ImageFactory.fromResource(R.drawable.map_marker_light)
                        )
                    )
                }

                nearbySelectedMap[marker!!.id] = place
                allMarkers += marker

            }
        }
        onNearbyMarkerClick(placesViewModel)
    }

    fun zoomOnNearbyMarkers(locations: List<SuggPlace>){
        if (locations.isEmpty()) return

        val avgLat = locations.map { it.latAndLng.latitude }.average()
        val avgLon = locations.map { it.latAndLng.longitude }.average()
        val center = GeoPoint(avgLat, avgLon)

        val cameraOptions = CameraOptions(
            position = center,
            zoom = 9.00
        )

        tomTomMap?.animateCamera(cameraOptions, 3.seconds)
    }

    private fun onNearbyMarkerClick(placesViewModel: PlacesViewModel){
        val selectedPlace by placesViewModel.selectedPlace

        nearbyMarkerClickListener = MarkerClickListener { marker ->
            val clickedPlace = nearbySelectedMap[marker.id]

            Log.d("onNearbyMarkerClick", clickedPlace.toString())

            if (clickedPlace == selectedPlace){
                marker.setPinIconImage(ImageFactory.fromResource(R.drawable.map_marker_light))
                placesViewModel.updateSelectedPlace(null)
            }else{
                selectedPlace?.let { old ->

                    val oldMarker = allMarkers.firstOrNull {
                        nearbySelectedMap[it.id] == old
                    }
                    oldMarker?.setPinIconImage(
                        ImageFactory.fromResource(R.drawable.map_marker_light)
                    )
                }
                val clickedMarker = allMarkers.firstOrNull {
                    nearbySelectedMap[it.id] == clickedPlace
                }
                clickedMarker?.setPinIconImage(ImageFactory.fromResource(R.drawable.map_marker_full))
                tomTomMap?.moveCamera(CameraOptions(position = GeoPoint(clickedPlace!!.latAndLng.latitude, clickedPlace.latAndLng.longitude), zoom = 15.0))
                placesViewModel.updateSelectedPlace(clickedPlace)
            }

        }
        true
        tomTomMap?.addMarkerClickListener(nearbyMarkerClickListener!!)
    }

    fun zoomOnNearbyMarker(place: SuggPlace, placesViewModel: PlacesViewModel){

        if (place != placesViewModel.selectedPlace.value){
            if(placesViewModel.selectedPlace.value != null){
                placesViewModel.selectedPlace.value?.let { old ->

                    val oldMarker = allMarkers.firstOrNull {
                        nearbySelectedMap[it.id] == old
                    }
                    oldMarker?.setPinIconImage(
                        ImageFactory.fromResource(R.drawable.map_marker_light)
                    )
                }
            }
            val clickedMarker = allMarkers.firstOrNull {
                nearbySelectedMap[it.id] == place
            }
            clickedMarker?.setPinIconImage(ImageFactory.fromResource(R.drawable.map_marker_full))
            placesViewModel.updateSelectedPlace(place)
        }
            tomTomMap?.moveCamera(CameraOptions(position = GeoPoint(place.latAndLng.latitude, place.latAndLng.longitude), zoom = 15.0))

    }

    fun removeMarkers(viewModel: PlacesViewModel){
        tomTomMap?.removeMarkerClickListener(nearbyMarkerClickListener!!)
        tomTomMap?.removeMarkers("nearby")
        allMarkers.clear()
        viewModel.clearPlaces()
    }




}
