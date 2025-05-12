package com.example.roadtripbuddy.mocks

import com.example.roadtripbuddy.NavigationMap
import io.mockk.mockk

object MockNavigationMap {
    val instance: NavigationMap = mockk(relaxed = true)
}