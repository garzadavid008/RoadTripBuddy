package com.example.roadtripbuddy.mocks

import androidx.compose.runtime.mutableStateOf
import com.example.roadtripbuddy.PlanATripViewModel
import com.example.roadtripbuddy.WaypointItem
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.Date

object MockPlanATripViewModel {
    val instance: PlanATripViewModel = mockk(relaxed = true) {
        every { planWaypoints } returns MutableStateFlow(emptyList<WaypointItem>())
        every { ETA } returns MutableStateFlow("")
        every { initialDeparture } returns MutableStateFlow(Date())
        every { selectedRoutePair } returns mutableStateOf(null)
        every { selectedWaypoint } returns mutableStateOf(null)
    }
}