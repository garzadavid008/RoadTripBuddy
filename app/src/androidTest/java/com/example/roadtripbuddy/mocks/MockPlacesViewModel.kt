package com.example.roadtripbuddy.mocks

import androidx.compose.runtime.mutableStateOf
import com.example.roadtripbuddy.PlacesViewModel
import com.example.roadtripbuddy.SuggPlace
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow

object MockPlacesViewModel {
    val instance: PlacesViewModel = mockk(relaxed = true) {
        every { restaurants } returns MutableStateFlow(emptyList<SuggPlace>())
        every { gas } returns MutableStateFlow(emptyList<SuggPlace>())
        every { entertainment } returns MutableStateFlow(emptyList<SuggPlace>())
        every { selectedPlace } returns mutableStateOf(null)
    }
}