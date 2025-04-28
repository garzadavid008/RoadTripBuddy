package com.example.roadtripbuddy

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.first

class SuggestedLocation(private val placesViewModel: PlacesViewModel) {

    // we need to cache the
    var foodList by mutableStateOf<List<SuggPlace>>(emptyList())
        private set
    var entertainment by mutableStateOf<List<SuggPlace>>(emptyList())
        private set

    var gasAndService by mutableStateOf<List<SuggPlace>>(emptyList())
        private set

    // hard coded user ratings
    var rating = 3.0
    var numberOfUserRating = 50 // 100 and above is considered popular

    // make a function that loads the user rating from the firestore
    fun loadQuizResult() {
        // firestore
    }

    // make a funtion that calls SearchNearBy
    suspend fun setUpList(
        latitude: Double,
        longitude: Double,
        includedTypes: MutableList<String>,
        category: String
    ) {
        // calling the api
        placesViewModel.getNearbyRestaurants(category, latitude, longitude, includedTypes)
        // waiting for the api call to finish
        when (category) {
            "food" ->{
                foodList = placesViewModel.restaurants.first { it.isNotEmpty() }
            }
            "gas" ->{
                gasAndService = placesViewModel.gas.first { it.isNotEmpty() }

            }
            "fun" -> entertainment = placesViewModel.entertainment.first { it.isNotEmpty() }
        }
    }

    // filter out based on the user needs from quiz
    fun filter(listFood: List<SuggPlace>): List<SuggPlace> {
        val filtered = mutableListOf<SuggPlace>()

        for (food in listFood) {
            val isPopularEnough = food.popular >= numberOfUserRating
            val isRatedHighEnough = food.rating >= rating

            if (isPopularEnough && isRatedHighEnough) {
                filtered.add(food)
            }
        }

        return filtered
    }


    suspend fun filterList() {
        foodList = foodList.filter { it.popular >= numberOfUserRating && it.rating >= rating }
        entertainment = entertainment.filter { it.popular >= numberOfUserRating && it.rating >= rating }
        gasAndService = gasAndService.filter { it.popular >= numberOfUserRating && it.rating >= rating}
    }

}