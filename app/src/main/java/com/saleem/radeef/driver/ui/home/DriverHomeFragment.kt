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
import android.content.Intent
//import android.location.LocationRequest
import android.net.Uri
import android.os.Looper
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.saleem.radeef.driver.DriverHomeUiState
import com.saleem.radeef.util.MIN_UPDATE_DISTANCE_METERS
import com.saleem.radeef.util.UiState
import com.saleem.radeef.util.calculateFee
import com.saleem.radeef.util.formatCost
import com.saleem.radeef.util.formatDistance
import com.saleem.radeef.util.hide
import com.saleem.radeef.util.logD
import com.saleem.radeef.util.show
import com.saleem.radeef.util.toLatLng
import configureLocationButton
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import saudiArabiaBounds
import setCameraBoundsAndZoom
import java.util.Locale
import kotlin.Exception

@AndroidEntryPoint
class DriverHomeFragment : Fragment(R.layout.driver_fragment_home), OnMapReadyCallback,
    EasyPermissions.PermissionCallbacks, GoogleMap.OnMyLocationButtonClickListener {
    private lateinit var map: GoogleMap
    lateinit var binding: DriverFragmentHomeBinding
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var currentLocation: LatLng
    val viewModel: DriverHomeViewModel by activityViewModels()

    private var isImageLoaded = false

    lateinit var header: View

    private val adapter by lazy {
        PassengerRequestsAdapter(requireContext())
    }


    var dist = 0.0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DriverFragmentHomeBinding.bind(view)


        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        setNavigationDrawer()

        observer()

    }


    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap



        setCurrentLocation()


        map.isMyLocationEnabled = true


        configureLocationButton(view)


        configureMapSettings(map)
        setCameraBoundsAndZoom(map, saudiArabiaBounds)

        map.setOnMyLocationButtonClickListener(this)
    }

    @SuppressLint("MissingPermission")
    private fun setCurrentLocation() {
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())


        val locationRequest = LocationRequest.Builder(1000)
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .setMinUpdateDistanceMeters(MIN_UPDATE_DISTANCE_METERS)
            .build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val lastLocation = locationResult.lastLocation
                if (lastLocation != null) {
                    val latitude = lastLocation.latitude
                    val longitude = lastLocation.longitude


                    viewModel.updateLocation(lastLocation.toLatLng())

                    val newLocation = LatLng(latitude, longitude)
                    logD("New current location: $latitude, $longitude")
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(newLocation, 15f))
                } else {
                    toast("last location is null")
                }
            }
        }

        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    private fun observer() {

        binding.menuButton.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        binding.pickupEt.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                val action =
                    DriverHomeFragmentDirections.actionDriverHomeFragmentToDriverSearchFragment()
                findNavController().navigate(action)
            }
        }


        viewModel.driver.observe(viewLifecycleOwner) { state ->
            when (state) {
                UiState.Loading -> {
                    // Show loading progress if needed
                }

                is UiState.Success -> {
                    if (!isImageLoaded) {
                        loadImage(state.data.personalPhotoUrl)
                        isImageLoaded = true
                    }

                    val nameTf = header.findViewById<TextView>(R.id.name_label)
                    nameTf.text = state.data.name

                    logD(state.data.toString())
                }

                is UiState.Failure -> {
                    // Handle failure state if needed
                    toast(state.error.toString())
                }
            }
        }
        observeCurrentHomeState()


        lifecycleScope.launch {
            viewModel.homeEvent.collect { event ->
                when (event) {
                    is DriverHomeViewModel.HomeEvent.CallPassenger -> {
                        makePhoneCall(event.phoneNumber)
                    }

                    else -> {
                        logD("in homeEvent collect: nothing here")
                    }
                }

            }
        }
    }

    private fun observeCurrentHomeState() {
        viewModel.currentHomeState.observe(viewLifecycleOwner) { state ->
            logD("in currentHomeState observer")
            when (state) {
                DriverHomeUiState.SettingPlaces -> {
                    logD("setting places: initial state")
                    binding.pickupIl.show()
                }

                is DriverHomeUiState.DisplayDriverPlaces -> {
                    //drawLineOnMap(viewModel.driverData?.pickupLatLng, viewModel.driverData?.destinationLatLng)
                    logD("setting places: display driver places")
                    displayDriverPlaces(state)
                }

                DriverHomeUiState.SearchingForPassengers -> {
                    logD("searching state!")
                    searchingForPassengers()
                }

                is DriverHomeUiState.WaitPassengerResponse -> {
                    logD("waiting response state!")
                    waitingPassengerResponse(state)
                }

                is DriverHomeUiState.PassengerPickUp -> {
                    passengerPickup(state)
                }

                is DriverHomeUiState.EnRoute -> {
                    enRoute(state)
                }

                is DriverHomeUiState.Arrived -> {
                    logD("arrived status!")
                    arrived(state)
                }

                is DriverHomeUiState.ContinueRide -> {
                    logD("continue state!")
                    continueRide(state)
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

    private fun setNavigationDrawer() {

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
    }

    private fun makePhoneCall(phoneNumber: String) {
        logD("make a phone call")
        val intent = Intent(Intent.ACTION_DIAL)
        intent.data = Uri.parse("tel:$phoneNumber")
        startActivity(intent)
    }

    private fun displayDriverPlaces(state: DriverHomeUiState.DisplayDriverPlaces) {
        hideAllViews()
        map.clear()
        drawLine(state.driverLatLng, state.driverDestinationLatLng)
        binding.pathDetailsView.pathDetailsLayout.show()
        binding.pathDetailsView.apply {
            pickupTitleTextView.setText(R.string.current_location)
            destinationTitleTextView.text = getAddressFromLatLng(state.driverDestinationLatLng)
            distanceTextView.text = state.distance.formatDistance()
        }

        binding.pathDetailsView.searchButton.setOnClickListener {
            logD("search button clicked!")
            viewModel.onSearchButtonClicked()
        }

        binding.pathDetailsView.changeDestinationTextView.setOnClickListener {
            val action =
                DriverHomeFragmentDirections.actionDriverHomeFragmentToDriverSearchFragment()
            findNavController().navigate(action)
        }
    }

    private fun searchingForPassengers() {
        hideAllViews()
        map.clear()

        val driver = viewModel.driverData!!
        //val anotherAdapter = PassengerRequestsAdapter(requireContext())
//        adapter.getDistance = { pickup, destination ->
//            viewModel.calculateDistance(pickup, destination)
//        }
        adapter.getCost = { distance ->
            calculateFee(distance)
        }

        adapter.onItemClick = { item ->
            viewModel.onAdapterItemClicked(item)
            map.clear()
            drawLine(
                driver.pickupLatLng,
                item.ride.passengerPickupLatLng,
                R.color.md_theme_light_secondary
            )
            drawLine(item.ride.passengerPickupLatLng, item.ride.passengerDestLatLng)
            drawLine(
                item.ride.passengerDestLatLng,
                driver.destinationLatLng,
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
        binding.ridesRequestView.ridesRequestLayout.show()

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

        binding.ridesRequestView.doneButton.setOnClickListener {
            viewModel.onDoneButtonClicked()
        }
    }

    private fun hideAllViews() {
        binding.apply {
            pathDetailsView.pathDetailsLayout.hide()
            ridesRequestView.ridesRequestLayout.hide()
            waitingPassengerView.waitingPassengerLayout.hide()
            passengerPickupView.passengerPickupLayout.hide()
            enRouteView.enRouteLayout.hide()
            passengerArrivedView.passengerArrivedLayout.hide()
            continueRideView.continueRideLayout.hide()
            pickupIl.hide()
        }
    }

    private fun waitingPassengerResponse(state: DriverHomeUiState.WaitPassengerResponse) {
        hideAllViews()
        map.clear()
        val driver = viewModel.driverData!!
        drawLine(
            driver.pickupLatLng,
            state.passengerPickupLatLng,
            R.color.md_theme_light_secondary
        )
        drawLine(state.passengerPickupLatLng, state.ride.passengerDestLatLng)
        drawLine(
            state.ride.passengerDestLatLng,
            driver.destinationLatLng,
            R.color.md_theme_light_secondary
        )
        binding.waitingPassengerView.apply {
            passengerNameTextView.text = state.passengerName
            pickupTextView.text = getAddressFromLatLng(state.passengerPickupLatLng)
            destinationTextView.text = getAddressFromLatLng(state.passengerDestinationLatLng)
            distanceTextView.text = state.distance.formatDistance()
            costTextView.text = state.cost.formatCost()
        }
        binding.waitingPassengerView.waitingPassengerLayout.show()

        binding.waitingPassengerView.cancelButton.setOnClickListener {
            viewModel.onCancelButtonClickedWaiting(state.ride)
        }

        binding.waitingPassengerView.callButton.setOnClickListener {
            viewModel.onCallPassenger(state.passengerId)
        }
    }

    private fun passengerPickup(state: DriverHomeUiState.PassengerPickUp) {
        logD("passengerPickup UI state!")
        hideAllViews()
        val driver = viewModel.driverData!!
        drawLine(
            driver.pickupLatLng,
            state.ride.passengerPickupLatLng,
            R.color.md_theme_light_primary
        )
        binding.passengerPickupView.passengerPickupLayout.show()
        val ride = state.ride
        binding.passengerPickupView.apply {
            passengerNameTextView.text = ride.passengerName
            pickupTextView.text = getAddressFromLatLng(ride.passengerPickupLatLng)
            distanceTextView.text = state.distance.formatDistance()
        }

        binding.passengerPickupView.callButton.setOnClickListener {
            viewModel.onCallPassenger(ride.passengerID)
        }

        binding.passengerPickupView.cancelButton.setOnClickListener {
            viewModel.onCancelButton(ride)
        }

        binding.passengerPickupView.arrivedButton.setOnClickListener {
            viewModel.onDriverArrivedToPassenger(ride, state)
        }
//        toast("picking up passenger")
    }

    private fun enRoute(state: DriverHomeUiState.EnRoute) {
        hideAllViews()
        map.clear()
        val ride = state.ride
        val driver = viewModel.driverData!!
        drawLine(
            //ride.passengerPickupLatLng,
            driver.pickupLatLng,
            ride.passengerDestLatLng,
            R.color.md_theme_light_primary
        )
        binding.enRouteView.enRouteLayout.show()

        binding.enRouteView.apply {
            passengerNameTextView.text = ride.passengerName
            destinationTextView.text = getAddressFromLatLng(ride.passengerDestLatLng)
            distanceTextView.text = state.distance.formatDistance()
        }

        binding.enRouteView.callButton.setOnClickListener {
            viewModel.onCallPassenger(ride.passengerID)
        }

        binding.enRouteView.completeButton.setOnClickListener {
            viewModel.onArrivedToPassengerDestination(ride, state)
        }
    }

    private fun arrived(state: DriverHomeUiState.Arrived) {
        logD("arrived state")
        hideAllViews()
        map.clear()
        val ride = state.ride
        val driver = viewModel.driverData!!
        drawLine(
            ride.passengerDestLatLng,
            driver.destinationLatLng,
            R.color.md_theme_light_primary
        )

        binding.passengerArrivedView.passengerArrivedLayout.show()
        binding.passengerArrivedView.apply {
            passengerNameTextView.text = ride.passengerName
            costTextView.text = ride.chargeAmount.formatCost()
        }

        binding.passengerArrivedView.confirmButton.setOnClickListener {
            viewModel.onContinueButtonClicked()
        }

        binding.passengerArrivedView.stopButton.setOnClickListener {
            viewModel.onStopButtonClicked()
        }
    }

    private fun continueRide(state: DriverHomeUiState.ContinueRide) {
        logD("CONTINUE status!")
        binding.passengerArrivedView.passengerArrivedLayout.hide()
        map.clear()
        val driver = viewModel.driverData!!
        drawLine(
            driver.pickupLatLng,
            driver.destinationLatLng,
            R.color.md_theme_light_primary
        )
        binding.continueRideView.apply {
            destinationTextView.text = getAddressFromLatLng(driver.destinationLatLng)
            distanceTextView.text = state.distance.formatDistance()
            continueRideLayout.show()
        }

        binding.continueRideView.doneButton.setOnClickListener {
            logD("done button clicked!")
            viewModel.onStopButtonClicked()
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
                //toast(dist.toString())

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

//                val startMarkerOptions = MarkerOptions()
//                    .position(decodedPolyline.first())
//                    .title("Start")
//
//                map.addMarker(startMarkerOptions)

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

            //map.addMarker(MarkerOptions().position(lastLocation))
        }
    }


    override fun onMyLocationButtonClick(): Boolean {
        //setMarker()
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
        try {
            val geocoder = Geocoder(requireContext(), Locale.getDefault())
            logD("getAddressFromLatLng: $latlng")
            val addresses = geocoder.getFromLocation(latlng.latitude, latlng.longitude, 1)
            return addresses?.firstOrNull()?.featureName ?: ""
        } catch (e: Exception) {
            logD("get address error: ${e.message}")
            return ""
        }
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