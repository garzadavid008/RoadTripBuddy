package com.example.roadtripbuddy

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
// the UI will call the view model to get the data.
class PlacesViewModel(private val placesClient: PlacesClient) : ViewModel() {

    private val repository = GooglePlacesRepository(placesClient)

    private val _restaurants = MutableStateFlow<List<SuggPlace>>(emptyList())
    // the ui will grab this
    val restaurants: StateFlow<List<SuggPlace>> get() = _restaurants
// function to call the api and returns a list of places. the UI
    fun getNearbyRestaurants(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            val result = repository.fetchNearbyRestaurants(latitude, longitude)
            _restaurants.value = result
        }
    }
    // same function as above but for Text Search
    fun getTextSearch(location:String,latitude: Double, longitude: Double) {
        viewModelScope.launch {
            val result = repository.textSearch(location,latitude, longitude)
            Log.i("Chris", "Updating StateFlow with ${result.size} places")
            _restaurants.value = result.toList()
        }
    }

}
