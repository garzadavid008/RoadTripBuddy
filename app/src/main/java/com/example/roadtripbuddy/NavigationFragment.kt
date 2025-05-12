package com.example.roadtripbuddy

import com.tomtom.sdk.navigation.TomTomNavigation
import com.tomtom.sdk.navigation.RoutePlan
import com.tomtom.sdk.navigation.ui.NavigationFragment as TomTomNavigationFragment
import com.tomtom.sdk.navigation.ui.NavigationUiOptions
import com.tomtom.sdk.routing.route.Route
import com.tomtom.sdk.routing.options.RoutePlanningOptions
import com.tomtom.sdk.routing.options.Itinerary
import com.tomtom.sdk.routing.options.ItineraryPoint
import com.tomtom.sdk.routing.options.guidance.GuidanceOptions
import com.tomtom.sdk.routing.*
import com.tomtom.sdk.datamanagement.navigationtile.NavigationTileStore
import com.tomtom.sdk.datamanagement.navigationtile.NavigationTileStoreConfiguration
import com.tomtom.sdk.navigation.online.Configuration
import com.tomtom.sdk.location.Place
import com.tomtom.sdk.navigation.online.OnlineTomTomNavigationFactory
import com.tomtom.sdk.navigation.ProgressUpdatedListener
import com.tomtom.sdk.navigation.ActiveRouteChangedListener
import com.tomtom.sdk.map.display.camera.CameraOptions
import com.tomtom.sdk.map.display.common.screen.Padding

class NavigationFragment(
    private val context: android.content.Context,
    private val activity: MainActivity,
    private val locationService: LocationService,
    private val searchManager: SearchManager,
    private val routeManager: RouteManager,
    private val apiKey: String
) {

    private fun Route.mapInstructions(): List<com.tomtom.sdk.map.display.route.Instruction> {
        val routeInstructions = legs.flatMap { it.instructions }
        return routeInstructions.map {
            com.tomtom.sdk.map.display.route.Instruction(routeOffset = it.routeOffset)
        }
    }

    var isClosed: Boolean = false
    private var isNavigationRunning = false

    private lateinit var tomTomNavigationFragment: TomTomNavigationFragment
    private lateinit var tomTomNavigation: TomTomNavigation
    private lateinit var navigationTileStore: NavigationTileStore

    companion object {
        fun newInstance(
            context: android.content.Context,
            activity: MainActivity,
            locationService: LocationService,
            searchManager: SearchManager,
            routeManager: RouteManager,
            apiKey: String,
            navigationUiOptions: NavigationUiOptions
        ): NavigationFragment {
            val fragment = NavigationFragment(context, activity, locationService, searchManager, routeManager, apiKey)
            fragment.tomTomNavigationFragment = TomTomNavigationFragment.newInstance(navigationUiOptions)
            return fragment
        }
    }

    fun createRouteAndStart(viewModel: SearchDrawerViewModel) {
        val waypoints = viewModel.waypoints.value
        if (waypoints.isEmpty()) return

        val origin = locationService.getLocationProvider().lastKnownLocation?.position
            ?: searchManager.startLocation
            ?: return
        val destination = waypoints.lastOrNull()?.place?.coordinate ?: return
        val intermediatePoints = waypoints.dropLast(1).mapNotNull { it?.place?.coordinate }

        val itinerary = Itinerary(
            origin = ItineraryPoint(Place(origin)),
            destination = ItineraryPoint(Place(destination)),
            waypoints = intermediatePoints.map { ItineraryPoint(Place(it)) }
        )

        val options = RoutePlanningOptions(
            itinerary = itinerary,
            guidanceOptions = GuidanceOptions()
        )

        routeManager.routePlanner.planRoute(options, object : RoutePlanningCallback {
            override fun onSuccess(result: RoutePlanningResponse) {
                val route = result.routes.firstOrNull() ?: return
                startNavigation(route, options)
            }

            override fun onFailure(failure: RoutingFailure) {}

            override fun onRoutePlanned(route: Route) = Unit
        })
    }

    fun stopNavigation() {
        if (!isNavigationRunning) return
        isNavigationRunning = false

        if (this::tomTomNavigationFragment.isInitialized) {
            tomTomNavigationFragment.stopNavigation()
            val transaction = activity.supportFragmentManager.beginTransaction()
            transaction.remove(tomTomNavigationFragment)
            transaction.commitNowAllowingStateLoss()
        }

        locationService.getTomTomMap()?.clear()
        locationService.resetUserLiveMarker()
        locationService.stopLiveTracking()

        resetMapPadding()

        locationService.getTomTomMap()?.cameraTrackingMode = com.tomtom.sdk.map.display.camera.CameraTrackingMode.None
        locationService.getTomTomMap()?.enableLocationMarker(
            com.tomtom.sdk.map.display.location.LocationMarkerOptions(
                com.tomtom.sdk.map.display.location.LocationMarkerOptions.Type.Pointer
            )
        )

        locationService.getTomTomMap()?.let {
            val lastKnown = locationService.getLocationProvider().lastKnownLocation?.position
            if (lastKnown != null) {
                it.moveCamera(CameraOptions(position = lastKnown, zoom = 10.0))
            }
        }
    }

    private val progressUpdatedListener = ProgressUpdatedListener {
        locationService.getTomTomMap()?.routes?.firstOrNull()?.progress = it.distanceAlongRoute

    }

    private val activeRouteChangedListener = ActiveRouteChangedListener { route ->
        locationService.getTomTomMap()?.removeRoutes()
        locationService.getTomTomMap()?.let { map ->
            val routeOptions = com.tomtom.sdk.map.display.route.RouteOptions(
                geometry = route.geometry,
                destinationMarkerVisible = true,
                departureMarkerVisible = true,
                instructions = route.mapInstructions(),
                routeOffset = route.routePoints.map { it.routeOffset }
            )
            map.addRoute(routeOptions)
        }
    }

    private fun startNavigation(route: Route, options: RoutePlanningOptions) {
        navigationTileStore = NavigationTileStore.create(
            context,
            NavigationTileStoreConfiguration(apiKey)
        )

        val config = Configuration(
            context = context,
            navigationTileStore = navigationTileStore,
            locationProvider = locationService.getLocationProvider(),
            routePlanner = routeManager.routePlanner
        )

        tomTomNavigation = OnlineTomTomNavigationFactory.create(config)

        val transaction = activity.supportFragmentManager.beginTransaction()
        if (!tomTomNavigationFragment.isAdded) {
            transaction.replace(R.id.fragment_container_view_tag, tomTomNavigationFragment)
            transaction.commitNow()
        }

        tomTomNavigationFragment.setTomTomNavigation(tomTomNavigation)
        tomTomNavigationFragment.startNavigation(RoutePlan(route, options))

        tomTomNavigationFragment.addNavigationListener(object : TomTomNavigationFragment.NavigationListener {


            override fun onStarted() {
                isNavigationRunning = true
                locationService.getTomTomMap()?.cameraTrackingMode = com.tomtom.sdk.map.display.camera.CameraTrackingMode.FollowRouteDirection
                locationService.getTomTomMap()?.enableLocationMarker(
                    com.tomtom.sdk.map.display.location.LocationMarkerOptions(
                        com.tomtom.sdk.map.display.location.LocationMarkerOptions.Type.Chevron
                    )
                )
                setMapNavigationPadding()
                tomTomNavigation.addProgressUpdatedListener(progressUpdatedListener)
                tomTomNavigation.addActiveRouteChangedListener(activeRouteChangedListener)
            }

            override fun onStopped() {
                stopNavigation()
            }
        })
    }

    private fun setMapNavigationPadding() {
        val paddingBottom = context.resources.getDimensionPixelOffset(R.dimen.map_padding_bottom)
        val padding = Padding(0, 0, 0, paddingBottom)
        locationService.getTomTomMap()?.setPadding(padding)
    }

    private fun resetMapPadding() {
        locationService.getTomTomMap()?.setPadding(Padding(0, 0, 0, 0))
        locationService.getTomTomMap()?.cameraTrackingMode = com.tomtom.sdk.map.display.camera.CameraTrackingMode.None

    }


}
