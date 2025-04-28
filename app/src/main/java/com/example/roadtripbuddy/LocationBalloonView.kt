package com.example.roadtripbuddy

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.tomtom.sdk.map.display.marker.BalloonViewAdapter
import com.tomtom.sdk.map.display.marker.Marker

@Suppress("unused")
class LocationBalloonView(private val context: Context) : BalloonViewAdapter {
    override fun onCreateBalloonView(marker: Marker): View {
        val view = LayoutInflater.from(context).inflate(R.layout.location_balloon_view, null)

        val balloonLocation = view.findViewById<TextView>(R.id.balloon_location)
        val balloonTimeSpent = view.findViewById<TextView>(R.id.balloon_time_spent)
        balloonLocation.text = marker.balloonText
        balloonTimeSpent.text = marker.tag
        return view
    }
}