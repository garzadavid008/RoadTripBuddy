package com.example.roadtripbuddy

import android.content.Context
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.core.graphics.blue
import androidx.core.graphics.luminance
import androidx.lifecycle.viewModelScope
import com.tomtom.sdk.annotations.InternalTomTomSdkApi
import com.tomtom.sdk.common.android.redFloat
import com.tomtom.sdk.location.Place
import com.tomtom.sdk.map.display.TomTomMap
import com.tomtom.sdk.map.display.image.ImageFactory
import com.tomtom.sdk.map.display.marker.Marker
import com.tomtom.sdk.map.display.marker.MarkerOptions
import com.tomtom.sdk.map.display.route.RouteClickListener
import com.tomtom.sdk.map.display.route.RouteOptions
import com.tomtom.sdk.routing.RoutePlanningCallback
import com.tomtom.sdk.routing.RoutePlanningResponse
import com.tomtom.sdk.routing.RoutingFailure
import com.tomtom.sdk.routing.online.OnlineRoutePlanner
import com.tomtom.sdk.routing.options.Itinerary
import com.tomtom.sdk.routing.options.ItineraryPoint
import com.tomtom.sdk.routing.options.RoutePlanningOptions
import com.tomtom.sdk.routing.options.calculation.CostModel
import com.tomtom.sdk.routing.options.guidance.GuidanceOptions
import com.tomtom.sdk.routing.route.Route
import com.tomtom.sdk.vehicle.Vehicle
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Date
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.math.roundToInt
import kotlin.time.Duration

class PlanRouteManager(context: Context, apiKey: String) {

    private val routePlanner = OnlineRoutePlanner.create(context, apiKey)
    private val routeMarkers = mutableMapOf<String, Marker>()
    private var routeClickListener: RouteClickListener? = null
    private val displayRoutes = mutableMapOf<String, Route >()


    private suspend fun planRouteAsync(options: RoutePlanningOptions): Route =
        suspendCancellableCoroutine { cont ->
            routePlanner.planRoute(options, object : RoutePlanningCallback {
                override fun onSuccess(response: RoutePlanningResponse) {
                    response.routes.firstOrNull()
                        ?.let { cont.resume(it) }
                        ?: cont.resumeWithException(IllegalStateException("No route returned"))
                }

                override fun onFailure(failure: RoutingFailure) {
                    cont.resumeWithException(RuntimeException(failure.message))
                }

                override fun onRoutePlanned(route: Route) = Unit
            })
            cont.invokeOnCancellation {
                // Optionally cancel the SDK request if supported
            }
        }

    //As it says, method to output the route on the map
    @OptIn(InternalTomTomSdkApi::class)
    private fun drawRoutes(
        tomTomMap: TomTomMap?, // Needs a tomTomMap to draw on
        routeList: List<Route>, // Needs a route
        viewModel: PlanATripViewModel, // Intakes an instance of the TripViewModel in order to update the estimated time of arrival
        withZoom: Boolean = true, // Optional parameter
    ) {

        displayRoutes.clear()

        // Initialing the total of route ETA's to zero
        var routeTotalETA: Duration = Duration.ZERO

        routeClickListener?.let { tomTomMap?.removeRouteClickListener(it) }

        routeList.forEachIndexed { index, route ->

            // for each route add the travel time to routeTotalETA
            routeTotalETA = routeTotalETA.plus(route.summary.travelTime)

            // This simply makes sure the first point has a start icon and the last point has an end icon
            val routeOptions = when {
                routeList.size == 1 -> RouteOptions(
                    geometry = route.geometry,
                    departureMarkerVisible = true,
                    destinationMarkerVisible = true,
                    routeOffset = route.routePoints.map { it.routeOffset },
                    color = RouteOptions.DEFAULT_COLOR,
                    tag = route.id.toString()
                )

                index == 0 -> RouteOptions(
                    geometry = route.geometry,
                    departureMarkerVisible = true, // first route gets a start marker
                    routeOffset = route.routePoints.map { it.routeOffset },
                    color = RouteOptions.DEFAULT_COLOR,
                    tag = route.id.toString()
                )

                index == routeList.lastIndex -> RouteOptions(
                    geometry = route.geometry,
                    destinationMarkerVisible = true, // last route gets an end marker
                    departureMarkerVisible = true,
                    departureMarkerPinImage = ImageFactory.fromResource(R.drawable.map_marker),
                    routeOffset = route.routePoints.map { it.routeOffset },
                    color = RouteOptions.DEFAULT_COLOR,
                    tag = route.id.toString()
                )

                else -> RouteOptions(
                    geometry = route.geometry,
                    departureMarkerVisible = true, // intermediate route: you might choose a default marker
                    departureMarkerPinImage = ImageFactory.fromResource(R.drawable.map_marker),
                    routeOffset = route.routePoints.map { it.routeOffset },
                    color = RouteOptions.DEFAULT_COLOR,
                    tag = route.id.toString()
                )
            }
            val disRoute = tomTomMap?.addRoute(routeOptions)
            Log.d("disRoute grabbed", disRoute.toString())
            if (disRoute != null){
                displayRoutes[disRoute.id.toString()] = route
            }
        }


        viewModel.updateETA(routeTotalETA)

        if (withZoom) {
            tomTomMap?.zoomToRoutes(ZOOM_TO_ROUTE_PADDING)
        }


        routeClickListener = RouteClickListener { routeClick ->
            onRouteLegClick(routeClick = routeClick, viewModel = viewModel, tomTomMap = tomTomMap)
            true
        }


        tomTomMap?.addRouteClickListener(routeClickListener!!)


    }

    companion object {
        private const val ZOOM_TO_ROUTE_PADDING = 100
    }

    /*
    private fun getTrafficColor(magnitude: MagnitudeOfDelay?): Int? {
        return when (magnitude) {
            MagnitudeOfDelay.MAJOR -> Color.Red.hashCode()
            MagnitudeOfDelay.MODERATE -> Color.Yellow.hashCode()
            MagnitudeOfDelay.MINOR -> Color.Yellow.hashCode()
            //MagnitudeOfDelay.INDEFINITE -> Color.Black.hashCode()
            else -> null // Default color when no delay info is available
        }
    }

     */

    //Calculates a route based on a list of waypoints from the PlanATripViewModel
    fun planOnRouteRequest(
        viewModel: PlanATripViewModel,
        tomTomMap: TomTomMap?
    ) {
        viewModel.viewModelScope.launch {
            val waypoints = viewModel.planWaypoints.value
            if (waypoints.size < 2) return@launch

            val routes = mutableListOf<Route>()
            var currentDepartAt: Date? = viewModel.initialDeparture.value

            for (i in 0 until waypoints.size - 1) {
                val fromWp = waypoints[i]
                val toWp = waypoints[i + 1]

                val dwellMillis = ((fromWp.hour * 60) + fromWp.minute) * 60 * 1000 // convert hours + minutes to ms

                currentDepartAt = Date(currentDepartAt!!.time + dwellMillis)

                Log.d("Debug ", "Departure time for ${fromWp.searchResult?.place?.address?.freeformAddress}: ${currentDepartAt}")

                val options = RoutePlanningOptions(
                    itinerary = Itinerary(
                        origin = ItineraryPoint(Place(coordinate = fromWp.searchResult!!.place.coordinate, address = fromWp.searchResult!!.place.address)),
                        destination = ItineraryPoint(Place(toWp.searchResult!!.place.coordinate, address = toWp.searchResult!!.place.address))
                    ),
                    departAt = currentDepartAt,
                    costModel = CostModel(),
                    guidanceOptions = GuidanceOptions(),
                    vehicle = Vehicle.Car()
                )

                try {
                    val route = planRouteAsync(options)
                    routes += route

                    // advance departAt by travelTime (in seconds)
                    val travelMillis = route.summary.travelTime.inWholeMilliseconds

                    currentDepartAt = Date(currentDepartAt!!.time + travelMillis)
                } catch (e: Exception) {
                    Log.e("Routing", "Failed leg $i: ${e.message}")
                }
            } // Take a look at the viewModel for time spent and then output route ballons

            // draw once all legs are done
            drawRoutes(tomTomMap, routes, viewModel)
        }
    }

    fun onRouteLegClick(
        routeClick: com.tomtom.sdk.map.display.route.Route,
        viewModel: PlanATripViewModel,
        tomTomMap: TomTomMap?
    ){
        val displayRoute = viewModel.selectedRoutePair.value?.first
        val matchingRoute = displayRoutes[routeClick.id.toString()]
        val routeId = matchingRoute?.id.toString()

        // If this route was already selected, deselect it
        if (displayRoute?.id == routeClick.id) {
            routeClick.color = RouteOptions.DEFAULT_COLOR
            viewModel.setSelectedRoutePair(null)

            // Remove the marker if it exists
            routeMarkers[routeId]?.let { tomTomMap?.removeMarkers(it.tag) }
            routeMarkers.remove(routeId)
        }
        else if (matchingRoute != null) {
            // If another route was selected previously, reset it
            displayRoute.let {
                it?.color = RouteOptions.DEFAULT_COLOR
                val oldRoute = displayRoutes[it?.id.toString()]
                val oldId = oldRoute?.id.toString()
                routeMarkers[oldId]?.let { marker -> tomTomMap?.removeMarkers(marker.tag) }
                routeMarkers.remove(oldId)
            }

            // Select the new route
            viewModel.setSelectedRoutePair(Pair(routeClick, matchingRoute))
            routeClick.color = Color.Blue.hashCode() // or Color.Blue.hashCode()
            routeClick.bringToFront()

            val miles = matchingRoute.summary.length.inMiles()
            val rounded = (miles * 10).roundToInt() / 10.0
            val midpoint = matchingRoute.geometry[matchingRoute.geometry.size / 2]

            val marker = tomTomMap?.addMarker(
                MarkerOptions(
                    coordinate = midpoint,
                    pinImage = ImageFactory.fromResource(R.drawable.line_pointer),
                    balloonText = "${matchingRoute.summary.travelTime.inWholeMinutes} min",
                    tag = "$rounded mi"
                )
            )
            marker?.select()
            if (marker != null) {
                routeMarkers[routeId] = marker
            }
        }
    }
}