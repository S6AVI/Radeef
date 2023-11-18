package com.saleem.radeef.driver.ui.home

import android.annotation.SuppressLint
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.DirectionsApi
import com.google.maps.GeoApiContext
import com.google.maps.android.PolyUtil
import com.google.maps.model.TravelMode
import com.google.maps.model.Unit
import com.saleem.radeef.R
import com.saleem.radeef.databinding.DriverFragmentHomeBinding
import com.saleem.radeef.passenger.ui.map.TAG
import com.saleem.radeef.util.toast
import com.vmadalin.easypermissions.EasyPermissions
import configureMapSettings
import RIYADH
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.saleem.radeef.data.RadeefLocation
import com.saleem.radeef.driver.DriverHomeUiState
import com.saleem.radeef.util.UiState
import com.saleem.radeef.util.calculateFee
import com.saleem.radeef.util.hide
import com.saleem.radeef.util.logD
import com.saleem.radeef.util.show
import configureLocationButton
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import saudiArabiaBounds
import setCameraBoundsAndZoom
import java.lang.Exception
import java.util.Locale

@AndroidEntryPoint
class DriverHomeFragment : Fragment(R.layout.driver_fragment_home), OnMapReadyCallback,
    EasyPermissions.PermissionCallbacks, GoogleMap.OnMyLocationButtonClickListener {
    private lateinit var map: GoogleMap
    lateinit var binding: DriverFragmentHomeBinding
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var currentLocation: LatLng
    val viewModel: DriverHomeViewModel by activityViewModels()
    private lateinit var polyline: Polyline

    private val drawnPolylines: MutableList<Polyline> = mutableListOf()

    lateinit var header: View

    private val adapter by lazy {
        PassengerRequestsAdapter(requireContext())
    }


    var dist = 0.0

    private val preferences: SharedPreferences by lazy {
        requireContext().getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DriverFragmentHomeBinding.bind(view)


        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)




        binding.menuButton.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
//            val action = HomeFragmentDirections.actionHomeFragmentToNavigationDrawerFragment()
//            findNavController().navigate(action)
        }


        header = binding.navigationView.getHeaderView(0)

        binding.navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_item_profile -> {
                    val action =
                        DriverHomeFragmentDirections.actionDriverHomeFragmentToDriverProfileFragment()
                    findNavController().navigate(action)
                }

                R.id.nav_item_wallet -> {
                    val action =
                        DriverHomeFragmentDirections.actionDriverHomeFragmentToDriverWalletFragment()
                    findNavController().navigate(action)
                }

                R.id.nav_item_payment -> {
                    val action =
                        DriverHomeFragmentDirections.actionDriverHomeFragmentToDriverPaymentFragment()
                    findNavController().navigate(action)

                }

                R.id.nav_item_rides -> {
                    val action =
                        DriverHomeFragmentDirections.actionDriverHomeFragmentToDriverRidesFragment()
                    findNavController().navigate(action)
                }

                R.id.nav_item_help -> {
                    val action =
                        DriverHomeFragmentDirections.actionDriverHomeFragmentToDriverHelpFragment()
                    findNavController().navigate(action)
                }

                R.id.nav_item_Settings -> {
                    //val action = HomeFragmentDirections.actionHomeFragmentToSettingsFragment2()
                    val action =
                        DriverHomeFragmentDirections.actionDriverHomeFragmentToDriverSettingsFragment()
                    findNavController().navigate(action)
                }
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            true
        }


        binding.pickupEt.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                logD("in home fragment: line 127 - pickup: ${viewModel.pickup?.latLng}")
                logD("in home fragment: line 128 - current: $currentLocation")
                val action =
                    DriverHomeFragmentDirections.actionDriverHomeFragmentToDriverSearchFragment(
                        currentLocation
                    )
                findNavController().navigate(action)
            }
        }


        viewModel.driver.observe(viewLifecycleOwner) { state ->
            when (state) {
                UiState.Loading -> {
//                    binding.progressBar.show()
//                    binding.continueBt.setText("")
                }

                is UiState.Success -> {
                    // binding.progressBar.hide()
                    //setIfReady()
                    loadImage(state.data.personalPhotoUrl)
                    val nameTf = header.findViewById<TextView>(R.id.name_label)
                    nameTf.text = state.data.name

                    logD(state.data.toString())

                }

                is UiState.Failure -> {
//                    binding.progressBar.hide()
//                    binding.continueBt.enable()
//                    binding.continueBt.setText(getString(R.string.continue_label))
                    toast(state.error.toString())
                }

            }
        }

        viewModel.currentHomeState.observe(viewLifecycleOwner) { state ->
            logD("in currentHomeState observer")
            when (state) {
                DriverHomeUiState.SettingPlaces -> {
                    logD("setting places: initial state")
                }

                is DriverHomeUiState.DisplayDriverPlaces -> {
                    //drawLineOnMap(viewModel.driverData?.pickupLatLng, viewModel.driverData?.destinationLatLng)
                    logD("setting places: display driver places")
                    binding.pickupIl.hide()
                    drawLineOnMap(state.driverLatLng, state.driverDestinationLatLng)
                    displayDriverPlaces(state)
                }

                DriverHomeUiState.SearchingForPassengers -> {
                    logD("searching state!")
                    searchingForPassengers()

                }

                is DriverHomeUiState.WaitPassengerResponse -> {
                    logD("waiting response state!")
                    waitingPassengerResponse()
                }

                is DriverHomeUiState.Arrived -> {
                    logD("waiting response state!")
                    arrived()
                }

                is DriverHomeUiState.ContinueRide -> {
                    logD("waiting response state!")
                    continueRide()
                }

                is DriverHomeUiState.EnRoute -> {
                    enRoute()
                }

                is DriverHomeUiState.PassengerPickUp -> {
                    passengerPickup()
                }

                DriverHomeUiState.Error -> {
                    logD("Home state is: Error")
                }

                DriverHomeUiState.Loading -> {
                    logD("Home state is: Loading")
                }
            }
        }
    }

    private fun displayDriverPlaces(state: DriverHomeUiState.DisplayDriverPlaces) {
        binding.pathDetailsView.pathDetailsLayout.show()
        binding.pathDetailsView.apply {
            pickupTitleTextView.text = viewModel.driverData?.pickup_title
            destinationTitleTextView.text = viewModel.driverData?.destination_title
            try {
                distanceTextView.text =
                    calculateDistance(state.driverLatLng, state.driverDestinationLatLng).toString()
            } catch (e: Exception) {
                logD(e.message.toString())
            }

        }

        binding.pathDetailsView.searchButton.setOnClickListener {
            logD("search button clicked!")
            viewModel.onSearchButtonClicked()
        }
    }

    private fun searchingForPassengers() {
        binding.waitingPassengerView.waitingPassengerLayout.hide()
        //val anotherAdapter = PassengerRequestsAdapter(requireContext())
//        adapter.getDistance = { pickup, destination ->
//            viewModel.calculateDistance(pickup, destination)
//        }
        adapter.getCost = { distance ->
            calculateFee(distance)
        }

        adapter.onItemClick = { item ->
            viewModel.onAdapterItemClicked(item)

            //viewModel.removePolylines()
            map.clear()
            drawLine(
                currentLocation,
                item.ride.passengerPickupLatLng,
                R.color.md_theme_light_secondary
            )
            drawLine(item.ride.passengerPickupLatLng, item.ride.passengerDestLatLng)
            drawLine(
                item.ride.passengerDestLatLng,
                viewModel.driverData!!.destinationLatLng,
                R.color.md_theme_light_secondary
            )

            logD("item clicked: ${item.ride.rideID}")
        }

        adapter.onAccept = { item, cost ->
            viewModel.onAdapterRideAccept(item, cost)
            logD("accept ride: ${item.ride.rideID}")
        }

        adapter.onHide = { id ->
            viewModel.onAdapterRideHide(id)
            logD("hide ride: $id")
        }


        binding.ridesRequestView.requestsRecyclerView.adapter = adapter
        binding.pathDetailsView.pathDetailsLayout.hide()
        binding.ridesRequestView.ridesRequestLayout.show()
        binding.ridesRequestView.noRequestsTextView.show()
        logD("we are searching!")

        viewModel.fetchRideRequests()
        viewModel.rideRequests.observe(viewLifecycleOwner) { uiState ->
            when (uiState) {
                is UiState.Loading -> {
                    binding.ridesRequestView.progressBar.show()
                }

                is UiState.Success -> {
                    binding.ridesRequestView.progressBar.hide()
                    binding.ridesRequestView.requestsRecyclerView.show()
                    val filteredRides = uiState.data
                    logD("length of filtered rides: ${filteredRides.size}")
                    logD("available ride requests: $filteredRides")
                    if (filteredRides.isEmpty()) {
                        binding.ridesRequestView.noRequestsTextView.show()
                    } else {
                        binding.ridesRequestView.noRequestsTextView.hide()
                    }
                    adapter.updateList(filteredRides.toMutableList())
                    //anotherAdapter.submitList(filteredRides)
                }

                is UiState.Failure -> {
                    binding.ridesRequestView.progressBar.hide()
                    val errorMessage = uiState.error.toString()
                    logD("error: $errorMessage")
                    // Handle error state
                }
            }
        }
    }

    private fun drawLine(start: LatLng, end: LatLng, color: Int = R.color.md_theme_light_primary) {
        val context = GeoApiContext.Builder()
            .apiKey(getString(R.string.google_maps_key))
            .build()

        val lineColor = ContextCompat.getColor(requireContext(), color)

        lifecycleScope.launch(Dispatchers.Main) {
            try {

                val directions = DirectionsApi.newRequest(context)
                    .origin(
                        com.google.maps.model.LatLng(
                            start.latitude,
                            start.longitude
                        )
                    )
                    .destination(
                        com.google.maps.model.LatLng(
                            end.latitude,
                            end.longitude
                        )
                    )
                    .mode(TravelMode.DRIVING)
                    .units(Unit.METRIC)

                val result = withContext(Dispatchers.IO) {
                    directions.await() // Perform the network request asynchronously on IO dispatcher
                }

                dist = result.routes[0].legs.sumOf { it.distance.inMeters } / 1000.0
                logD("distance: $dist")
                toast(dist.toString())

                val route = result.routes[0]

                // Get the polyline data from the route
                val encodedPolyline = route.overviewPolyline.encodedPath

                val decodedPolyline = PolyUtil.decode(encodedPolyline)

                val boundsBuilder = LatLngBounds.Builder()
                for (point in decodedPolyline) {
                    boundsBuilder.include(point)
                }
                val bounds = boundsBuilder.build()

                val polylineOptions = PolylineOptions()
                    .addAll(decodedPolyline)
                    .color(lineColor)
                    .width(10f)

                // Add the polyline to the map
                val polyline = map.addPolyline(polylineOptions)

                val startMarkerOptions = MarkerOptions()
                    .position(decodedPolyline.first())
                    .title("Start")

                map.addMarker(startMarkerOptions)

                val endMarkerOptions = MarkerOptions()
                    .position(decodedPolyline.last())
                    .title("End")
                map.addMarker(endMarkerOptions)

            } catch (e: Exception) {
                Log.d(TAG, "draw line: some error occurred")
                Log.d(TAG, e.toString())
            }
        }
    }

    private fun calculateCost(PassengerPickup: LatLng, PassengerDestination: LatLng): Double {
        TODO("Not yet implemented")
    }

    private fun passengerPickup() {
        TODO("Not yet implemented")
    }

    private fun enRoute() {
        TODO("Not yet implemented")
    }

    private fun continueRide() {
        TODO("Not yet implemented")
    }

    private fun arrived() {
        TODO("Not yet implemented")
    }

    private fun waitingPassengerResponse() {
        binding.ridesRequestView.ridesRequestLayout.hide()
        binding.waitingPassengerView.waitingPassengerLayout.show()
        logD("waiting: Not yet implemented")
    }


//    private fun setIfReady() {
//        val isReady = preferences.getBoolean("isReady", false)
//        logD("isReady: $isReady")
//
//        if (isReady) {
//            if (viewModel.pickup != null && viewModel.destination != null) {
//                logD("in first branch: ${viewModel.pickup?.latLng}")
//                drawLineOnMap(viewModel.pickup!!.latLng, viewModel.destination!!.latLng)
//                //showPathDetailsBottomSheet()
//            } else {
//                logD("in second branch: ${viewModel.driverData}")
//                drawLineOnMap(viewModel.driverData?.pickupLatLng, viewModel.driverData?.destinationLatLng)
//                //showPathDetailsBottomSheet()
//            }
//        }
//    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap


        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())
        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                // Animate the camera to the user's current location
                currentLocation = LatLng(location.latitude, location.longitude)

                val title = getAddressFromLatLng(currentLocation)
                viewModel.pickup = RadeefLocation(currentLocation, title)

                map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f))
            } else {
                // If the user's location is not available, animate the camera to Riyadh
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(RIYADH, 15f))
            }
        }

        map.addMarker(MarkerOptions().position(RIYADH).title("Riyadh"))
        map.animateCamera(CameraUpdateFactory.newLatLng(RIYADH))
        map.isMyLocationEnabled = true

        // place my-location button on bottom-right-corner
        configureLocationButton(view)


        configureMapSettings(map)
        setCameraBoundsAndZoom(map, saudiArabiaBounds)

        map.setOnMyLocationButtonClickListener(this)


        Log.d(TAG, viewModel.pickup.toString())
        if (viewModel.pickup != null && viewModel.destination != null) {
            //val pickupLatLng = getLatLngFromAddress(viewModel.pickup.latLng!!)
            val pickupLatLng = viewModel.pickup?.latLng
            val destinationLatLng = viewModel.destination?.latLng
            drawLineOnMap(pickupLatLng!!, destinationLatLng!!)

        }

    }

//    private fun showPathDetailsBottomSheet() {
//        logD("in showPathDetailsBottomSheet")
//        val action = DriverHomeFragmentDirections.actionDriverHomeFragmentToDriverPathDetailsFragment(dist.toFloat())
//        findNavController().navigate(action)
//    }

    private fun drawLineOnMap(pickupLatLng: LatLng?, destinationLatLng: LatLng?) {

        val context = GeoApiContext.Builder()
            .apiKey(getString(R.string.google_maps_key))
            .build()

        Log.d(TAG, context.toString())
        Log.d(TAG, pickupLatLng.toString())
        Log.d(TAG, destinationLatLng.toString())
        lifecycleScope.launch(Dispatchers.Main) {
            try {
                Log.d(TAG, "before directions init")
                val directions = DirectionsApi.newRequest(context)
                Log.d(TAG, directions.toString())
                directions.origin(
                    com.google.maps.model.LatLng(
                        pickupLatLng!!.latitude,
                        pickupLatLng.longitude
                    )
                )
                Log.d(TAG, "line: 213: ${directions.toString()}")
                directions
                    .destination(
                        com.google.maps.model.LatLng(
                            destinationLatLng!!.latitude,
                            destinationLatLng.longitude
                        )
                    )
                Log.d(TAG, "line: 221: ${directions.toString()}")
                directions
                    .mode(TravelMode.DRIVING)
                    .units(Unit.METRIC)

                Log.d(TAG, "line: 249: before result")

                val result = withContext(Dispatchers.IO) {
                    directions.await() // Perform the network request asynchronously on IO dispatcher
                }
                Log.d(TAG, "line: 251: $result")
                dist = result.routes[0].legs.sumOf { it.distance.inMeters } / 1000.0
                logD("distance: $dist")
                toast(dist.toString())

                val route = result.routes[0]

                // Get the polyline data from the route
                val encodedPolyline = route.overviewPolyline.encodedPath

// Decode the polyline data into a list of LatLng objects
                val decodedPolyline = PolyUtil.decode(encodedPolyline)

                Log.d(TAG, "decodedPolyline size: ${decodedPolyline.size}")


// Move the camera to the bounding box of the polyline
                val boundsBuilder = LatLngBounds.Builder()
                for (point in decodedPolyline) {
                    boundsBuilder.include(point)
                }
                val bounds = boundsBuilder.build()

                val polylineOptions = PolylineOptions()
                    .addAll(decodedPolyline)
                    .color(R.color.md_theme_light_primary)
                    .width(10f)

                // Add the polyline to the map
                val polyline = map.addPolyline(polylineOptions)


                logD("bounds: ${bounds.center}")

                val startMarkerOptions = MarkerOptions()
                    .position(decodedPolyline.first())
                    .title("Start")

                map.addMarker(startMarkerOptions)

                val endMarkerOptions = MarkerOptions()
                    .position(decodedPolyline.last())
                    .title("End")
                map.addMarker(endMarkerOptions)


                val cameraPosition = CameraPosition.Builder()
                    .zoom(12.0f) //  desired zoom level as a float value
                    .bearing(0.0f) //  desired bearing angle as a float value
                    .tilt(90.0f) //  desired tilt angle as a float value
                    .target(decodedPolyline[decodedPolyline.size / 2])
                    .build()

                logD("before camera update: ${cameraPosition.target}")
                val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 100).let {
                    CameraUpdateFactory.newCameraPosition(cameraPosition)
                }

                map.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        pickupLatLng,
                        13f
                    )
                )

                //map.animateCamera(cameraUpdate)


                Log.d(TAG, "line: 226: ${directions.toString()}")

            } catch (e: Exception) {
                Log.d(TAG, "here")
                Log.d(TAG, e.toString())
                e.printStackTrace()
                val cause = e.cause
                if (cause != null) {
                    Log.d(TAG, "here")
                }
            }
        }


    }

    private fun getLatLngFromAddress(address: String): LatLng {
        val geocoder = Geocoder(requireContext())
        val results = geocoder.getFromLocationName(address, 1)
        val location = results?.get(0)!!
        return LatLng(location.latitude, location.longitude)


    }

    @SuppressLint("MissingPermission")
    fun setMarker() {

        fusedLocationProviderClient.lastLocation.addOnCompleteListener {
            val lastLocation = LatLng(
                it.result.latitude,
                it.result.longitude
            )

            map.addMarker(MarkerOptions().position(lastLocation))
        }
    }


    @SuppressLint("MissingPermission")
    private fun setCurrent() {
        fusedLocationProviderClient.lastLocation.addOnCompleteListener {
            val lastLocation = LatLng(
                it.result.latitude,
                it.result.longitude
            )
            currentLocation = lastLocation
        }
    }

    override fun onMyLocationButtonClick(): Boolean {
        setMarker()
        return false
    }

    private fun loadImage(uri: String) {
        Glide.with(requireContext())
            .load(uri)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .into(header.findViewById(R.id.profile_image))
    }

    private fun getAddressFromLatLng(latlng: LatLng): String {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        val addresses = geocoder.getFromLocation(latlng.latitude, latlng.longitude, 1)
        return addresses?.firstOrNull()?.featureName ?: ""
    }

    private fun calculateDistance(start: LatLng, end: LatLng): Double {

        val directionsContext = GeoApiContext.Builder()
            .apiKey(getString(R.string.google_maps_key))
            .build()

        var distance = 0.0
        logD("inside calc")
        logD("start Lat: ${start.latitude}, Lng: ${start.longitude}")
        logD("end Lat: ${end.latitude}, Lng: ${end.longitude}")
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val directions = DirectionsApi.newRequest(directionsContext)
                directions.origin(
                    com.google.maps.model.LatLng(
                        start.latitude,
                        start.longitude
                    )
                )
                directions
                    .destination(
                        com.google.maps.model.LatLng(
                            end.latitude,
                            end.longitude
                        )
                    )
                val result =
                    directions
                        .mode(TravelMode.DRIVING)
                        .units(Unit.METRIC)
                        .await()

                distance = result.routes[0].legs.sumOf { it.distance.inMeters } / 1000.0
                logD("distance inside coroutine: $distance")
                binding.pathDetailsView.distanceTextView.text = distance.toString()
            } catch (e: Exception) {
                Log.d(TAG, e.toString())
                e.printStackTrace()
            }
        }
        logD("distance in helper: $distance")

        return distance
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        viewModel.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        viewModel.onPermissionsDenied(this, requestCode, perms)
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        viewModel.onPermissionsGranted(this, requestCode, perms)
    }
}