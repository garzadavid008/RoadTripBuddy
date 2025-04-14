package com.example.roadtripbuddy
import PlacesViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.libraries.places.api.net.PlacesClient

class PlacesViewModelFactory(private val placesClient: PlacesClient) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PlacesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PlacesViewModel(placesClient) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
