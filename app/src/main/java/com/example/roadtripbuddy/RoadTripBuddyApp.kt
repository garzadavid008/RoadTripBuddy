package com.example.roadtripbuddy            // ← match your package name

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp          // <- this enables Hilt code-gen
class RoadTripBuddyApp : Application()