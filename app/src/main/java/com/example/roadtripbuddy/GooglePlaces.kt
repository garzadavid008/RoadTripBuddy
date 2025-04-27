package com.example.roadtripbuddy

import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.android.libraries.places.api.model.CircularBounds
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.api.net.SearchByTextRequest
import com.google.android.libraries.places.api.net.SearchNearbyRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
//import  com.google.android.libraries.places.api.net*
//import com.google.android.libraries.places.api.model.Place


// THIS MAKES THE API call
class GooglePlacesRepository(private val placesClient: PlacesClient) {

    suspend fun fetchNearbyRestaurants(latitude: Double, longitude: Double): List<SuggPlace> {
        return withContext(Dispatchers.IO) {
            try {

                // Define search area as a 600m diameter circle
                val center = LatLng(latitude, longitude)
                val circle = CircularBounds.newInstance(center, /* radius = */ 600.0)

                // Fields to retrieve from the API
                val placeFields = listOf(Place.Field.NAME, Place.Field.RATING,Place.Field.ADDRESS,Place.Field.LAT_LNG)

                //  including and exclusing types
                // this filters what we want
                val includedTypes = listOf("restaurant", "cafe")
               // val excludedTypes = listOf("pizza_restaurant", "american_restaurant")

                //  builds SearchNearbyRequest builder
                val request = SearchNearbyRequest.builder(circle, placeFields)
                    .setIncludedTypes(includedTypes)
                   // .setExcludedTypes(excludedTypes)
                    .setMaxResultCount(5)
                    .build()

                // Perform search synchronously using Tasks.await()
                val response = Tasks.await(placesClient.searchNearby(request))

                // Convert response to list of Place objects
                return@withContext response.places.map { place ->
                    SuggPlace(
                        name = place.name ?: "Unknown",
                        rating = place.rating ?: -1.0,
                        address = place.address ?: "No address", // if its null put no address
                        latAndLng = place.latLng ?: LatLng(0.0,0.0)
                    )
                }
            } catch (e: Exception) {
                Log.e("API_ERROR", "Failed to fetch restaurants: ${e.message}")
                return@withContext emptyList()
            }
        }
    }

    // function to get a specific location near a user with location bias as well
    // will still use SuggPlace class
    suspend fun textSearch(location:String,latitude: Double, longitude: Double): List<SuggPlace> {
        return withContext(Dispatchers.IO) {
            try {

                // defining the cirlce and its cords
                val center = LatLng(latitude, longitude)
                val circle = CircularBounds.newInstance(center, /* radius = */ 50000.0)

                // Fields to retrieve from the API
                val placeFields = listOf(Place.Field.NAME, Place.Field.RATING,Place.Field.ADDRESS,Place.Field.LAT_LNG,Place.Field.TYPES)

                //  including and exclusing types
                // this filters what we want
                //val includedTypes = listOf("restaurant")
                //  builds SearchNearbyRequest builder
                val request = SearchByTextRequest.builder(location, placeFields)
                    .setMaxResultCount(5)
                    .setLocationBias(CircularBounds.newInstance(center,600.0))
                    .build()

                // Perform search synchronously using Tasks.await()
                val response = Tasks.await(placesClient.searchByText(request))

                // Convert response to list of Place objects
                return@withContext response.places.map { place ->
                    SuggPlace(
                        name = place.name ?: "Unknown",
                        rating = place.rating ?: -1.0,
                        address = place.address ?: "No address found", // if its null
                        latAndLng = place.latLng ?: LatLng(0.0,0.0),
                        category = place.types?.firstOrNull()?.name ?: "unknown"
                    )
                }
            } catch (e: Exception) {
                Log.e("API_ERROR", "Failed to fetch restaurants: ${e.message}")
                return@withContext emptyList()
            }
        }
    }

// function to get the PLACE ID so we can feed it into fetchPlace() to get the address, name, types , and phone number




}
// data class to hold the suggested locations data
// latAndLng holds a LatLng object that contains the latitude and longitude
data class SuggPlace(val name: String, val rating: Double, val address:String,val latAndLng:LatLng, val category: String = "Unknown")
