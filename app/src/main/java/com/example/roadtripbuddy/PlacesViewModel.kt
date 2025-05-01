package com.example.roadtripbuddy

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.roadtripbuddy.GooglePlacesRepository
import com.example.roadtripbuddy.pages.listofLATandLong
import com.google.android.gms.maps.model.PointOfInterest
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.example.roadtripbuddy.SuggPlace
// the UI will call the view model to get the data.
class PlacesViewModel(private val placesClient: PlacesClient) : ViewModel() {

    private val repository = GooglePlacesRepository(placesClient)

    private val _restaurants = MutableStateFlow<List<SuggPlace>>(emptyList())
    // the ui will grab this
    val restaurants: StateFlow<List<SuggPlace>> get() = _restaurants

    private val _gas = MutableStateFlow<List<SuggPlace>>(emptyList())
    // the ui will grab this
    val gas: StateFlow<List<SuggPlace>> get() = _gas

    private val _fun = MutableStateFlow<List<SuggPlace>>(emptyList())
    // the ui will grab this
    val entertainment: StateFlow<List<SuggPlace>> get() = _fun

    fun updateList(category: String, list :List<SuggPlace>){
        when(category)
        {
            "food"-> {
                _restaurants.value = list
            }
            "gas" ->{
                _gas.value = list
            }
            "entertainment"->{
                _fun.value = list
            }
        }
    }


    // function to call the api and returns a list of places. the UI
    fun getNearbyRestaurants(category: String,latitude: Double, longitude: Double,interest: MutableList<String>) {
        viewModelScope.launch {
            val result = repository.fetchNearbyRestaurants(latitude, longitude, interest)
            Log.i("Chris", "Updating StateFlow with ${result.size} places")

            when(category){
                "food" ->  _restaurants.value = result
                "gas" -> _gas.value = result
                "fun" -> _fun.value= result
            }

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