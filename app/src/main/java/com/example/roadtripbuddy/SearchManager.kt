package com.example.roadtripbuddy

import PlacesViewModel
import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewModelScope
import com.example.roadtripbuddy.SearchDrawer.SearchDrawerViewModel
import com.example.roadtripbuddy.pages.RightSidePanelDemo
import com.example.roadtripbuddy.pages.Suggestions
import com.google.android.libraries.places.api.Places
import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.location.Place
import com.tomtom.sdk.map.display.TomTomMap
import com.tomtom.sdk.map.display.camera.CameraOptions
import com.tomtom.sdk.map.display.image.ImageFactory
import com.tomtom.sdk.map.display.internal.C
import com.tomtom.sdk.map.display.marker.MarkerOptions
import com.tomtom.sdk.routing.options.Itinerary
import com.tomtom.sdk.routing.options.ItineraryPoint
import com.tomtom.sdk.routing.options.RoutePlanningOptions
import com.tomtom.sdk.routing.options.guidance.GuidanceOptions
import com.tomtom.sdk.search.*
import com.tomtom.sdk.search.autocomplete.*
import com.tomtom.sdk.search.common.error.SearchFailure
import com.tomtom.sdk.search.model.result.*
import com.tomtom.sdk.search.online.OnlineSearch
import com.tomtom.sdk.vehicle.Vehicle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.time.Duration


class SearchManager(context: Context, apiKey: String) {
    private val searchApi: Search = OnlineSearch.create(context, apiKey)
    var startLocation: GeoPoint? = null
    var fuzzySuggestionsPairs by mutableStateOf(emptyList<Pair<String, SearchResult?>>()) //For the resolveAndSuggest function

    fun updateStartLocation(location: GeoPoint?) {
        // Add any extra logic here if needed.
        startLocation = location
    }

    //Uses fuzzy search to simply convert a string address into a SearchResult object
    fun searchResultGetter(query: String, callback: (SearchResult?) -> Unit){
        //Log.d("BaseMap", query)
        val searchOptions = SearchOptions(query = query)

        searchApi.search(
            searchOptions,
            object : SearchCallback {
                override fun onSuccess(result: SearchResponse) {
                    callback(result.results.first()) //Returns a SearchObject as the callback
                }

                override fun onFailure(failure: SearchFailure) {
                    Log.e("SearchManager", "Search failed: ${failure.message}")
                    callback(null)
                }
            }
        )
    }



    // helper function so perform search can use Viewmodel and places client for the api calls
    fun createPlacesClientAndViewmodel(context:Context):PlacesViewModel{
        val placesClient = Places.createClient(context)
        return ViewModelProvider(context as ViewModelStoreOwner, PlacesViewModelFactory(placesClient))[PlacesViewModel::class.java]
    }
    // Method to either find, add marker, and zoom into an initial SearchResult, or if the query is
    // a brand/POI(Point of Interest) category, direct it to the locations nearby method
    fun performSearch(
        query: String,
        viewModel: SearchDrawerViewModel,
        context: Context,
        clearMap: () -> Unit,
        tomTomMap: TomTomMap?,
        planRouteAndGetETA: suspend (RoutePlanningOptions) -> Duration
    ) {
        if (query.isEmpty()) {
            Log.e("SearchManager", "Empty search query")
            return
        }

        // Before going any further we check if the string query is an address we can zoom in on or
        // a brand/POI(Point of Interest) category
        resolveAndSuggest(query, objectResult = { result ->
            if (result == null) {
                return@resolveAndSuggest
            }

            if (result is AutocompleteResult){ //If the object returned is a brand/poi category
                // locations nearby method CALL GOES HERE
                // val isBrand = result.segments.any{it is AutocompleteSegmentBrand}
                val brandName = result.segments.filterIsInstance<AutocompleteSegmentBrand>().firstOrNull()?.brand?.name.toString()
                Log.i("Chris"," Brand NAme : $brandName ")
               // val context = this@SearchManager
                val viewModel = createPlacesClientAndViewmodel(context)
                // grabbing the users current position
                val location = tomTomMap?.currentLocation?.position

                if(location != null)
                {

                    val lat = location.latitude.toDouble()
                    val long = location.longitude.toDouble()

                    CoroutineScope(Dispatchers.IO).launch {
                        viewModel.getTextSearch(brandName, lat, long) // Call the function
                        Log.i("Chris", "lat and long :  $lat $long")

                        // Switch to Main dispatcher for UI updates
                        withContext(Dispatchers.Main) {
                            viewModel.restaurants.collectLatest { placeList ->
                                Log.i("Chris", "list created : ${placeList.size}")

                                placeList.forEach { places ->
                                    Log.i("Chris", "Name : ${places.name}")
                                    val geoPoint = GeoPoint(places.latAndLng.latitude, places.latAndLng.longitude)

                                    val mark = MarkerOptions(
                                        coordinate = geoPoint,
                                        pinImage = ImageFactory.fromResource(R.drawable.map_marker)
                                    )
                                    tomTomMap.addMarker(mark)
                                }
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

                if (startLocation == null) {
                    Log.d("Debug", "startLocation is null")
                    return@resolveAndSuggest
                }

                tomTomMap?.addMarker(markerOptions)
                tomTomMap?.moveCamera(CameraOptions(locationGeoPoint, zoom = 15.0))

                val itinerary = Itinerary(
                    origin = ItineraryPoint(Place(startLocation!!)),
                    destination = ItineraryPoint(Place(locationGeoPoint)),
                    waypoints = emptyList()  // emptyList() because no waypoints are needed
                )
                val options = RoutePlanningOptions(
                    itinerary = itinerary,
                    guidanceOptions = GuidanceOptions(),
                    vehicle = Vehicle.Car()
                )

                // Launch a coroutine to update the ETA(estimated time of arrival) asynchronously
                // We do this because in order to get the ETA of a route, we need to plan a route so
                // we use RouteManagers method planRouteAndGetETA
                viewModel.viewModelScope.launch  {
                    try {
                        val etaDuration = planRouteAndGetETA(options)
                        viewModel.updateETA(etaDuration.toString())// Updates the searchDrawerViewModel ETA
                    } catch (e: Exception) {
                        Log.d("FAILURE", "Route planning failed: ${e.message}",)
                    }
                }
            }
        })
    }

    // For the resolveAndSuggest function, performs a simple fuzzy search and returns a list of pairs
    // EX: User types "Los Angeles", this would return a list of pairs like this
    // {["Los Angeles", SearchResult object], ["Los Altos", SearchResult object],......}
    private fun fuzzySearchAutocomplete(
        query: String,
        onResult: (List<Pair<String, SearchResult?>>) -> Unit = {}
    ) {

        if (query.isBlank()) {
            onResult(emptyList())
            return
        }

        val searchOptions = SearchOptions(
            query = query,
            locale = Locale("en", "US"),
            limit = 5,
            geoBias = startLocation
        )

        searchApi.search(
            searchOptions,
            object : SearchCallback {
                override fun onSuccess(result: SearchResponse) {
                    // Create pairs using freeformAddress if available; otherwise, fallback to a property like the place name
                    val combinedResults = result.results.map { suggestion ->
                        val display = suggestion.place.address?.freeformAddress ?: suggestion.place.name
                        Pair(display, suggestion)
                    }
                    Log.d("Debug", "fuzzySearchAutocomplete Results: $combinedResults")
                    onResult(combinedResults)
                }

                override fun onFailure(failure: SearchFailure) {
                    Log.d("FAILURE: fuzzySearchAutocomplete", failure.message)
                    onResult(emptyList())
                }
            }
        )
    }

    // A multi purpose function. From a string query it uses fuzzy search and autocomplete search to make
    // a list of pairs, each pair includes a string name and its corresponding object, the object
    // will either be a SearchResult(A State, City, or Address), or an AutocompleteResult(a brand or a POI category),
    // from that it returns two things, onResult returns a list of only the strings inside the pairs
    // and ObjectResult returns the first index object. onResult is for the autocomplete suggestions
    // under search bars, ObjectResult is for the performSearch method.
    fun resolveAndSuggest(
        query: String,
        tomTomMap: TomTomMap? = null,
        onResult: (List<String>) -> Unit = {},//An optional function parameter that returns a suggestion list of strings (this is for the search bar suggestions)
        objectResult: (Any?) -> Unit = {} //An optional function parameter that returns an object, either and AutocompleteResult or SearchResult
    ) {
        if (query.isBlank()) {
            onResult(emptyList()) // Return empty list for empty queries
            return
        }

        Log.d("Debug", "resolveAndSuggest query: $query")

        val autocompleteOptions = AutocompleteOptions(
            query = query,
            position = tomTomMap?.currentLocation?.position,
            locale = Locale("en", "US"),
            limit = 5
        )

        searchApi.autocompleteSearch(// first we use autocomplete search for brands and poi categories
            autocompleteOptions,
            object : AutocompleteCallback {
                override fun onSuccess(result: AutocompleteResponse) {
                    val autocompletePairs =
                        result.results.mapNotNull { res -> // we make the results be a list of Pairs, each pair being made up of the string name, the other being the object
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

                    Log.d("Debug", "autocompletePairs: $autocompletePairs")


                    // Call fuzzySearchAutocomplete to perform the fuzzy search.
                    fuzzySearchAutocomplete(query) { fuzzyPairs ->
                        fuzzySuggestionsPairs = fuzzyPairs
                        Log.d("Debug", "fuzzySuggestionsPairs before sorting: $fuzzySuggestionsPairs")

                        val combinedResults = (fuzzySuggestionsPairs + autocompletePairs)
                            .sortedWith(compareByDescending { pair ->
                                var score = 0f
                                if (pair.first.startsWith(query, ignoreCase = true)) score += 0.2f
                                score
                            })
                            .take(5)

                        objectResult(combinedResults.firstOrNull()?.second)
                        onResult(combinedResults.map { it.first })
                    }
                }

                override fun onFailure(failure: SearchFailure) {
                    Log.e("Autocomplete", "Error: ${failure.message}")
                    onResult(emptyList())
                }
            }
        )
    }

}