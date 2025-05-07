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
import kotlin.math.roundToInt

//import  com.google.android.libraries.places.api.net*
//import com.google.android.libraries.places.api.model.Place


// THIS MAKES THE API call
class GooglePlacesRepository(private val placesClient: PlacesClient) {


    private fun calculateDistance(
        orgLat: Double, orgLong:Double,
        deslat:Double, destLong: Double
    ): Double
    {
        val result = FloatArray(1)
        android.location.Location.distanceBetween(
            orgLat,orgLong,deslat,destLong,result
        )
        // formula to convert meters to miles
        val number = result[0] * 0.000621371
        return (number * 100.0).roundToInt() / 100.0
    }
    suspend fun fetchNearbyRestaurants(latitude: Double, longitude: Double, interest: MutableList<String>): List<SuggPlace> {
        return withContext(Dispatchers.IO) {
            try {

                // Define search area as a 600m diameter circle
                val center = LatLng(latitude, longitude)
                val circle = CircularBounds.newInstance(center, /* radius = */ 50000.0)

                // Fields to retrieve from the API
                val placeFields = listOf(Place.Field.NAME, Place.Field.RATING,Place.Field.ADDRESS,Place.Field.LAT_LNG,Place.Field.USER_RATINGS_TOTAL)

                //  including and exclusing types
                // this filters what we want
                val includedTypes = interest
                // val excludedTypes = listOf("pizza_restaurant", "american_restaurant")

                //  builds SearchNearbyRequest builder
                val request = SearchNearbyRequest.builder(circle, placeFields)
                    .setIncludedTypes(includedTypes)
                    // .setExcludedTypes(excludedTypes)
                    .setMaxResultCount(15)
                    .build()

                // Perform search synchronously using Tasks.await()
                val response = Tasks.await(placesClient.searchNearby(request))

                // Convert response to list of Place objects
                return@withContext response.places.map { place ->
                    val distMiles = calculateDistance(latitude,longitude,place.latLng.latitude,place.latLng.longitude)
                    SuggPlace(
                        name = place.name ?: "Unknown",
                        rating = place.rating ?: -1.0,
                        address = place.address ?: "No address", // if its null put no address
                        latAndLng = place.latLng ?: LatLng(0.0,0.0),
                        //user rating come here
                        popular = place.userRatingsTotal ?: 0,
                        distanceFromLocation = distMiles
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
                    .setLocationBias(CircularBounds.newInstance(center,100.0))
                    .build()

                // Perform search synchronously using Tasks.await()
                val response = Tasks.await(placesClient.searchByText(request))

                // Convert response to list of Place objects
                return@withContext response.places.map { place ->
                    val distMiles =
                        place.latLng?.let {
                            place.latLng?.let { it1 ->
                                calculateDistance(latitude,longitude,
                                    it.latitude, it1.longitude)
                            }
                        }
                    SuggPlace(
                        name = place.name ?: "Unknown",
                        rating = place.rating ?: -1.0,
                        address = place.address ?: "No address found", // if its null
                        latAndLng = place.latLng ?: LatLng(0.0,0.0),
                        category = place.types?.firstOrNull()?.name ?: "unknown",
                        distanceFromLocation = distMiles!!
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
data class SuggPlace(val name: String, val rating: Double, val address:String,val latAndLng:LatLng, val category: String = "Unknown",val distanceFromLocation:Double =0.0, val popular:Int=0)
