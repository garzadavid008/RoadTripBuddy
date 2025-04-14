package com.example.roadtripbuddy

import android.content.Context
import android.util.Log
import com.tomtom.sdk.location.Place
import com.tomtom.sdk.map.display.TomTomMap
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
import com.tomtom.sdk.vehicle.Vehicle
import kotlin.time.Duration

class PlanRouteManager(context: Context, apiKey: String) {

    private val routePlanner = OnlineRoutePlanner.create(context, apiKey)

    //As it says, method to output the route on the map
    private fun drawRoutes(
        tomTomMap: TomTomMap?, // Needs a tomTomMap to draw on
        routeList: List<Route>, // Needs a route
        viewModel: PlanATripViewModel, // Intakes an instance of the TripViewModel in order to update the estimated time of arrival
        color: Int = RouteOptions.DEFAULT_COLOR, // Optional parameter
        withZoom: Boolean = true, // Optional parameter
    ) {
        // Initialing the total of route ETA's to zero
        var routeTotalETA: Duration = Duration.ZERO

        Log.d("Debug", routeList.toString())

        routeList.forEachIndexed { index, route ->

            // for each route add the travel time to routeTotalETA
            routeTotalETA = routeTotalETA.plus(route.summary.travelTime)

            val instructions =
                route.legs
                    .flatMap { routeLeg -> routeLeg.instructions }
                    .map {
                        Instruction(
                            routeOffset = it.routeOffset,
                        )
                    }
            // This simply makes sure the first point has a start icon and the last point has an end icon
            val routeOptions =
                if (index == 0 && index == routeList.size - 1 ) {
                    RouteOptions(
                        geometry = route.geometry,
                        instructions = instructions,
                        departureMarkerVisible = true,// start marker if index is zero
                        destinationMarkerVisible = true, // destination marker if last index
                        routeOffset = route.routePoints.map { it.routeOffset },
                        color = color,
                        tag = route.id.toString(),
                    )
                } else if(index == 0) {
                    RouteOptions(
                        geometry = route.geometry,
                        instructions = instructions,
                        departureMarkerVisible = true,
                        routeOffset = route.routePoints.map { it.routeOffset },
                        color = color,
                        tag = route.id.toString(),
                    )
                } else if (index == routeList.size - 1) {
                    RouteOptions(
                        geometry = route.geometry,
                        instructions = instructions,
                        destinationMarkerVisible = true, // destination marker if last index
                        routeOffset = route.routePoints.map { it.routeOffset },
                        color = color,
                        tag = route.id.toString(),
                    )
                } else{
                    RouteOptions(
                        geometry = route.geometry,
                        instructions = instructions,
                        departureMarkerVisible = true,
                        routeOffset = route.routePoints.map { it.routeOffset },
                        color = color,
                        tag = route.id.toString(),
                    )
                }
            // add route leg
            tomTomMap?.addRoute(routeOptions)
        }

        viewModel.updateETA(routeTotalETA)

        if (withZoom) {
            tomTomMap?.zoomToRoutes(ZOOM_TO_ROUTE_PADDING)
        }
    }

    companion object {
        private const val ZOOM_TO_ROUTE_PADDING = 100
    }

    //Calculates a route based on a list of waypoints from the PlanATripViewModel
    fun planOnRouteRequest(
        viewModel: PlanATripViewModel,
        tomTomMap: TomTomMap?,
    ) {
        val waypointItemList =
            viewModel.planWaypoints.value //Grab the waypoint list from the viewModel
        val routeLegList = mutableListOf<RouteLeg>()
        val routeList = mutableListOf<Route>()

        // Check that there are at least two waypoints to connect
        if (waypointItemList.size >= 2) {
            for (i in 0 until waypointItemList.size - 1) {
                val fromWaypoint = waypointItemList[i]
                val toWaypoint = waypointItemList[i + 1]

                val leg = RouteLeg(
                    from = fromWaypoint,
                    to = toWaypoint,
                    eta = "0"
                )
                routeLegList.add(leg)
            }
        }

        routeLegList.forEachIndexed { index, leg ->
            val fromLocation = leg.from.searchResult?.place?.coordinate
            val toLocation = leg.to.searchResult?.place?.coordinate

            Log.d("DEBUG", fromLocation.toString())
            Log.d("DEBUG", toLocation.toString())


            val itinerary = Itinerary(
                origin = ItineraryPoint(Place(fromLocation!!)),
                destination = ItineraryPoint(Place(toLocation!!)), //last destination initialized in routeLocationsConstructor
            )
            Log.d("DEBUG", "ITINERARY SET")

            // Using the itinerary we just made, we initialize a routePlanningOptions
            val routePlanningOptions =
                RoutePlanningOptions(
                    itinerary = itinerary,
                    departAt = leg.from.departAt,
                    guidanceOptions = GuidanceOptions(),
                    vehicle = Vehicle.Car(),//This is set to change
                )

            // callback is a set up for the future, AKA when planRoute is called
            val callback = object : RoutePlanningCallback {
                //On planRoute success, map is drawn, and the viewModels ETA is updated
                override fun onSuccess(result: RoutePlanningResponse) {
                    val route = result.routes.first()
                    routeList.add(route)
                    leg.eta = route.summary.travelTime.toString()

                    if (index == routeLegList.size - 1){
                        drawRoutes(
                            tomTomMap = tomTomMap,
                            routeList = routeList,
                            viewModel = viewModel
                        )
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

    }
}