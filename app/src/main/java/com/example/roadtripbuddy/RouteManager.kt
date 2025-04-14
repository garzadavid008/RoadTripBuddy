package com.example.roadtripbuddy

import android.content.Context
import android.util.Log
import android.widget.Toast
//import com.example.roadtripbuddy.SearchDrawer.SearchDrawerViewModel
import androidx.compose.ui.graphics.Color
import com.tomtom.sdk.annotations.InternalTomTomSdkApi
import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.location.Place
import com.tomtom.sdk.map.display.TomTomMap
import com.tomtom.sdk.map.display.image.ImageFactory
import com.tomtom.sdk.map.display.marker.MarkerOptions
import com.tomtom.sdk.map.display.polyline.PolylineOptions
import com.tomtom.sdk.map.display.route.Instruction
import com.tomtom.sdk.map.display.route.RouteOptions
import com.tomtom.sdk.routing.RoutePlanningCallback
import com.tomtom.sdk.routing.RoutePlanningResponse
import com.tomtom.sdk.routing.RoutingFailure
import com.tomtom.sdk.routing.online.OnlineRoutePlanner
import com.tomtom.sdk.routing.options.Itinerary
import com.tomtom.sdk.routing.options.ItineraryPoint
import com.tomtom.sdk.routing.options.RoutePlanningOptions
import com.tomtom.sdk.routing.options.guidance.GuidanceOptions
import com.tomtom.sdk.routing.route.Route
import com.tomtom.sdk.routing.route.section.traffic.MagnitudeOfDelay
import com.tomtom.sdk.search.model.result.SearchResult
import com.tomtom.sdk.vehicle.Vehicle
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.time.Duration

class RouteManager(context: Context, apiKey: String) {

    private val routePlanner = OnlineRoutePlanner.create(context, apiKey)
    private var waypointList = mutableListOf<ItineraryPoint>() //For the route constructor, sets up waypoints for route itinerary
    private var lastDestination: GeoPoint? = null // For the route constructor, sets up the final destination for route itinerary
    private var route: Route? = null

    //As it says, method to output the route on the map
    @OptIn(InternalTomTomSdkApi::class)
    private fun drawRoute(
        tomTomMap: TomTomMap?, // Needs a tomTomMap to draw on
        route: Route, // Needs a route
        viewModel: SearchDrawerViewModel, // Intakes an instance of the TripViewModel in order to update the estimated time of arrival
        color: Int = RouteOptions.DEFAULT_COLOR, // Optional parameter
        withDepartureMarker: Boolean = true, // Optional parameter
        withZoom: Boolean = true, // Optional parameter
    ) {

        viewModel.updateETA(route.summary.travelTime.toString()) // Update the viewModels estimated time for arrival


        val instructions =
            route.legs
                .flatMap { routeLeg -> routeLeg.instructions }
                .map {
                    Instruction(
                        routeOffset = it.routeOffset,
                    )
                }
        val routeOptions =
            RouteOptions(
                geometry = route.geometry,
                destinationMarkerVisible = true,
                departureMarkerVisible = withDepartureMarker,
                instructions = instructions,
                routeOffset = route.routePoints.map { it.routeOffset },
                color = color,
                tag = route.id.toString(),
            )

        //Adds a marker for each waypoint
        for (waypoint in waypointList){
            val markerOptions = MarkerOptions(
                coordinate = waypoint.place.coordinate,
                pinImage = ImageFactory.fromResource(R.drawable.map_marker)
            )

            tomTomMap?.addMarker(markerOptions)
        }

        tomTomMap?.addRoute(routeOptions)

        for (section in route.sections.trafficSections) {
            val color = getTrafficColor(section.magnitudeOfDelay)
            if (color != null) {
                val polylineOptions = PolylineOptions(
                    coordinates = section.sectionLocation.geometry, // your list of coordinates
                    lineColor = color
                )
                tomTomMap?.addPolyline(polylineOptions)
            }
        }

        if (withZoom) {
            tomTomMap?.zoomToRoutes(ZOOM_TO_ROUTE_PADDING)
        }
    }

    companion object {
        private const val ZOOM_TO_ROUTE_PADDING = 100
    }


    private fun getTrafficColor(magnitude: MagnitudeOfDelay?): Int? {
        return when (magnitude) {
            MagnitudeOfDelay.MAJOR -> Color.Red.hashCode()
            MagnitudeOfDelay.MODERATE -> Color.Yellow.hashCode()
            MagnitudeOfDelay.MINOR -> Color.Yellow.hashCode()
            MagnitudeOfDelay.INDEFINITE -> Color.Black.hashCode()
            else -> null // Default color when no delay info is available
        }
    }

    //Calculates a route based on a list of waypoints from the TripViewModel
    fun onRouteRequest(
        viewModel: TripViewModel,
        tomTomMap: TomTomMap?,
        searchManager: SearchManager
    ) {
        val list = viewModel.waypoints.value //Grab the waypoint list from the viewModel

        // We call the constructor to organize the list
        routeLocationsConstructor(list = list)

        if (lastDestination == null) {
            Log.e("FAILURE", "Destination not found in onRouteRequest.")
            return@onRouteRequest
        }

        val startLocation =
            searchManager.startLocation!! //grab the startLocation from the searchManager class

        // Making an itinerary using TomTom's routing api is weird in that it NEEDS you to specify
        // the final destination and specify the waypoints, that's why we call the
        // routeLocationConstructor method
        val itinerary = Itinerary(
            origin = ItineraryPoint(Place(startLocation)),
            destination = ItineraryPoint(Place(lastDestination!!)), //last destination initialized in routeLocationsConstructor
            waypoints = waypointList //waypoint list initialized in routeLocationConstructor
        )

        // Using the itinerary we just made, we initialize a routePlanningOptions
        val routePlanningOptions =
            RoutePlanningOptions(
                itinerary = itinerary,
                guidanceOptions = GuidanceOptions(),
                vehicle = Vehicle.Car(),//This is set to change
            )

        // callback is a set up for the future, AKA when planRoute is called
        val callback = object : RoutePlanningCallback {
            //On planRoute success, map is drawn, and the viewModels ETA is updated
            override fun onSuccess(result: RoutePlanningResponse) {
                route = result.routes.first()
                route?.let {
                    // We call our drawRoute method
                    drawRoute(
                        tomTomMap = tomTomMap,
                        route = it,
                        viewModel = viewModel
                    )
                    // We update the viewModels ETA
                    viewModel.updateETA(it.summary.travelTime.toString())
                }
            }

            override fun onFailure(failure: RoutingFailure) {
                Log.d("FAILURE: RoutePlanningCallback", failure.message)
            }

            override fun onRoutePlanned(route: Route) = Unit
        }

        // Using the routePlanningOptions and callback values we just made, we call routePlanner's
        // planRoute method, this finalizes the routing
        routePlanner.planRoute(routePlanningOptions, callback)
    }


    suspend fun planRouteAndGetETA(options: RoutePlanningOptions): Duration =
        suspendCoroutine { cont ->
            routePlanner.planRoute(options, object : RoutePlanningCallback {
                override fun onSuccess(result: RoutePlanningResponse) {
                    val plannedRoute = result.routes.firstOrNull()
                    if (plannedRoute != null) {
                        cont.resume(plannedRoute.summary.travelTime)
                    } else {
                        cont.resumeWithException(Exception("No route found"))
                    }
                }
                override fun onFailure(failure: RoutingFailure) {
                    cont.resumeWithException(Exception(failure.message))
                }
                override fun onRoutePlanned(route: Route) = Unit
            })
        }

    // Constructs a list of waypoints by initializing the waypointList and the lastDestination
    // Why we need to do this? As said in onRouteRequest: Making an itinerary using TomTom's routing
    // api is weird in that it NEEDS you to specify the final destination and specify the waypoints
    private fun routeLocationsConstructor(
        list: List<SearchResult?>
    ) {

        if (list.isEmpty()) {
            return
        }
        waypointList.clear() //Old waypointList must be discarded, were creating a new one

        // If there's only one location, simply initialize it as the lastDestination
        if (list.size == 1) {
            lastDestination = list[0]?.place?.coordinate
            return // This might not be needed
        }

        list.forEachIndexed { index, location ->
            if (index < list.size - 1) {
                // We call our addWaypoint method
                val locationGeoPoint = location?.place?.coordinate
                waypointList.add(ItineraryPoint(Place(locationGeoPoint!!)))
            }
            else if (index == list.size - 1){
                lastDestination = location?.place?.coordinate
            }
        }
    }
}