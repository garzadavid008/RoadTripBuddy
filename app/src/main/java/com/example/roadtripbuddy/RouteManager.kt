package com.example.roadtripbuddy

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.roadtripbuddy.SearchDrawer.SearchDrawerViewModel
import com.tomtom.sdk.location.GeoPoint
import com.tomtom.sdk.location.Place
import com.tomtom.sdk.map.display.TomTomMap
import com.tomtom.sdk.map.display.image.ImageFactory
import com.tomtom.sdk.map.display.marker.MarkerOptions
import com.tomtom.sdk.map.display.route.Instruction
import com.tomtom.sdk.map.display.route.RouteOptions
import com.tomtom.sdk.routing.RoutePlanner
import com.tomtom.sdk.routing.RoutePlanningCallback
import com.tomtom.sdk.routing.RoutePlanningResponse
import com.tomtom.sdk.routing.RoutingFailure
import com.tomtom.sdk.routing.online.OnlineRoutePlanner
import com.tomtom.sdk.routing.options.Itinerary
import com.tomtom.sdk.routing.options.ItineraryPoint
import com.tomtom.sdk.routing.options.RoutePlanningOptions
import com.tomtom.sdk.routing.options.guidance.GuidanceOptions
import com.tomtom.sdk.routing.route.Route
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
    private fun drawRoute(
        tomTomMap: TomTomMap?, // Needs a tomTomMap to draw on
        route: Route, // Needs a route
        viewModel: SearchDrawerViewModel, // Intakes an instance of the SearchDrawerViewModel in order to update the estimated time of arrival
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
        if (withZoom) {
            tomTomMap?.zoomToRoutes(ZOOM_TO_ROUTE_PADDING)
        }
    }

    companion object {
        private const val ZOOM_TO_ROUTE_PADDING = 100
    }

    //Calculates a route based on a list of waypoints from the SearchDrawerViewModel
    fun onRouteRequest(
        viewModel: SearchDrawerViewModel,
        tomTomMap: TomTomMap?,
        searchManager: SearchManager
    ) {
        val list = viewModel.waypoints.value //Grab the waypoint list from the viewModel

        // Since routeLocationsConstructor is A constructor, we need to wait for it to finish, so we
        // use routeLocationsConstructors onComplete callback function to put our remaining code inside
        // of
        routeLocationsConstructor(
            list = list,
            searchManager = searchManager,
            onComplete = { // when the constructor is done ->
            if (lastDestination == null) {
                Log.e("FAILURE", "Destination not found in routeLocationsConstructor.")
                return@routeLocationsConstructor
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
        })
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

    //Adds a waypoint by taking a string address then converting it to a SearchResult Object, then to an ItineraryPoint
    private fun addWaypoint(
        query: String,
        onComplete: () -> Unit, // Callback to ensure this finishes before continuing
        searchManager: SearchManager
    ) {
        // Uses searchManagers searchResultGetter method to convert the string query to a SearchResult
        // object in order to obtain its corresponding GeoPoint. In other words String address goes in,
        // SearchResult object comes out
        searchManager.searchResultGetter(query) { newWaypoint -> // newWaypoint is the SearchResult object that comes out
            if (newWaypoint != null) { // if the newWaypoint is NOT null
                // add it to the waypointList by converting it to an ItineraryPoint
                waypointList.add(ItineraryPoint(Place(newWaypoint.place.coordinate)))
            } else {
                Log.e("FAILURE", "Failed to get a waypoint for query: $query")
            }
            onComplete() //Done
        }
    }

    // Constructs a list of waypoints by initializing the waypointList and the lastDestination
    // Why we need to do this? As said in onRouteRequest: Making an itinerary using TomTom's routing
    // api is weird in that it NEEDS you to specify the final destination and specify the waypoints
    private fun routeLocationsConstructor(
        list: List<String>,
        onComplete: () -> Unit, // Callback to ensure this method finishes before continuing
        searchManager: SearchManager
    ) {

        if (list.isEmpty()) {
            onComplete() //Done
            return
        }

        waypointList.clear() //Old waypointList must be discarded, were creating a new one

        // If there's only one location, simply initialize it as the lastDestination
        if (list.size == 1) {
            searchManager.searchResultGetter(list.first()) { searchResult ->
                if (searchResult != null) {
                    lastDestination = searchResult.place.coordinate
                } else {
                    Log.e("FAILURE", "AT routeLocationsContractor, Failed to set destination for: ${list.first()}")
                }
                onComplete()// Done
            }
            return // This might not be needed
        }

        // For all locations except the last, add them as waypoints.
        var completedCount = 0

        // we subtract the final destination from the list size because to TomTom routing api,
        // the last waypoint is not a waypoint
        val totalWaypoints = list.size - 1

        list.forEachIndexed { index, location ->
            if (index < list.size - 1) {
                // We call our addWaypoint method
                addWaypoint(
                    query = location,
                    searchManager = searchManager,
                    onComplete = { // When were done adding waypoints ->
                    completedCount++
                    if (completedCount == totalWaypoints) {
                        searchManager.searchResultGetter(list.last()) { searchResult ->
                            if (searchResult != null) {
                                lastDestination = searchResult.place.coordinate
                            } else {
                                Log.e("FAILURE", "Adding waypoints in routeLocationsConstructor: Failed to set destination for: ${list.last()}")
                            }
                            onComplete()// Done
                        }
                    }
                })

            }
        }
    }
}