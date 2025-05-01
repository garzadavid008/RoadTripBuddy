package com.example.roadtripbuddy

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.tomtom.sdk.map.display.marker.BalloonViewAdapter
import com.tomtom.sdk.map.display.marker.Marker

@Suppress("unused")
class RouteBalloonView(private val context: Context) : BalloonViewAdapter {
    override fun onCreateBalloonView(marker: Marker): View {
        val view = LayoutInflater.from(context).inflate(R.layout.route_balloon_view, null)

        val balloonTime = view.findViewById<TextView>(R.id.balloon_time)
        val balloonDistance = view.findViewById<TextView>(R.id.balloon_length)
        balloonTime.text = marker.balloonText
        balloonDistance.text = marker.tag
        return view
    }
}