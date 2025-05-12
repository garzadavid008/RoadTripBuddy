package com.example.roadtripbuddy.mocks

import com.example.roadtripbuddy.SearchDrawerViewModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow

object MockSearchDrawerViewModel {
    val instance: SearchDrawerViewModel = mockk(relaxed = true) {
        every { waypoints } returns MutableStateFlow(emptyList())
        every { ETA } returns MutableStateFlow("10 min")
    }
}