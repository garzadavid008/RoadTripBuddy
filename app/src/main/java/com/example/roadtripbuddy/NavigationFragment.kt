package com.example.roadtripbuddy

import android.util.Log
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

// -------------------------------------------------------------------------------------------------
// NavigationFragment - Manages Turn-by-Turn Navigation with TomTom SDK
// -------------------------------------------------------------------------------------------------

class NavigationFragment(
    private val context: android.content.Context,
    private val activity: MainActivity,
    private val locationService: LocationService,
    private val searchManager: SearchManager,
    private val routeManager: RouteManager,
    private val apiKey: String
) {
    // State
    private var isNavigationRunning = false

    // TomTom Navigation Components
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

    // -------------------------------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------------------------------

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

            override fun onFailure(failure: RoutingFailure) {
                Log.e("NavigationFragment", "Failed to plan route: ${failure.message}")
            }

            override fun onRoutePlanned(route: Route) = Unit
        })
    }

    fun stopNavigation() {
        if (!isNavigationRunning) {
            Log.i("NavigationFragment", "Navigation already stopped, nothing to do.")
            return
        }

        if (this::tomTomNavigation.isInitialized) {
            try {
                tomTomNavigation.stop()
                tomTomNavigation.close()
            } catch (e: IllegalStateException) {
                Log.w("NavigationFragment", "TomTomNavigation already closed.")
            }
        }

        if (this::navigationTileStore.isInitialized) {
            try {
                navigationTileStore.close()
            } catch (e: Exception) {
                Log.w("NavigationFragment", "Tile store already closed.")
            }
        }

        if (this::tomTomNavigationFragment.isInitialized) {
            try {
                tomTomNavigationFragment.stopNavigation()
            } catch (e: IllegalStateException) {
                Log.w("NavigationFragment", "NavigationFragment already stopped.")
            }
            try {
                val transaction = activity.supportFragmentManager.beginTransaction()
                transaction.remove(tomTomNavigationFragment)
                transaction.commitNowAllowingStateLoss()
            } catch (e: Exception) {
                Log.w("NavigationFragment", "NavigationFragment already removed.")
            }
        }

        locationService.getTomTomMap()?.clear()
        locationService.resetUserLiveMarker()

        locationService.stopLiveTracking()

        isNavigationRunning = false
    }

    // -------------------------------------------------------------------------------------------------
    // Private Helpers
    // -------------------------------------------------------------------------------------------------

    private fun startNavigation(route: Route, options: RoutePlanningOptions) {
        navigationTileStore = NavigationTileStore.create(
            context,
            NavigationTileStoreConfiguration(apiKey)
        )

        // No simulation, using actual navigation provider
        val config = Configuration(
            context = context,
            navigationTileStore = navigationTileStore,
            locationProvider = locationService.getLocationProvider(), // Directly using locationProvider
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
                Log.d("NavigationFragment", "Navigation started.")
            }

            override fun onStopped() {
                stopNavigation()
                Log.d("NavigationFragment", "Navigation stopped.")
            }
        })
    }
}
