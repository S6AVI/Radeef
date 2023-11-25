package com.saleem.radeef.passenger.ui.map

import android.annotation.SuppressLint
import android.location.Geocoder
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
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
import com.saleem.radeef.databinding.FragmentHomeBinding
import com.saleem.radeef.passenger.PassengerHomeUiState
import com.saleem.radeef.util.MIN_UPDATE_DISTANCE_METERS
import com.saleem.radeef.util.Permissions.hasLocationPermission
import com.saleem.radeef.util.Permissions.requestLocationPermission
import com.saleem.radeef.util.UiState
import com.saleem.radeef.util.hide
import com.saleem.radeef.util.logD
import com.saleem.radeef.util.show
import com.saleem.radeef.util.toLatLng
import com.saleem.radeef.util.toast
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog
import configureLocationButton
import configureMapSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import saudiArabiaBounds
import setCameraBoundsAndZoom
import java.util.Locale
import kotlin.Exception


class PassengerHomeFragment : Fragment(R.layout.fragment_home), OnMapReadyCallback,
    EasyPermissions.PermissionCallbacks, GoogleMap.OnMyLocationButtonClickListener {
    private lateinit var map: GoogleMap
    lateinit var binding: FragmentHomeBinding
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var currentLocation: LatLng
    val viewModel: PassengerHomeViewModel by activityViewModels()
    private lateinit var polyline: Polyline

    lateinit var header: View

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentHomeBinding.bind(view)

        if (hasLocationPermission(requireContext())) {
            val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
            mapFragment?.getMapAsync(this)
        } else {
            requestLocationPermission(this)
        }

        setNavigationDrawer()

        observer()



    }

    private fun observer() {
        binding.menuButton.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        viewModel.passenger.observe(viewLifecycleOwner) { state ->
            when (state) {
                UiState.Loading -> {

                }
                is UiState.Success -> {

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


        binding.pickupEt.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                val action =
                    PassengerHomeFragmentDirections.actionHomeFragmentToSearchFragment()
                findNavController().navigate(action)
            }
        }


    }

    private fun observeCurrentHomeState() {

        viewModel.currentHomeState.observe(viewLifecycleOwner) {state ->
            logD("in currentHomeState observer")
            when (state) {

                PassengerHomeUiState.SettingPlaces -> {
                    hideAllViews()
                    binding.pickupIl.show()
                }

                is PassengerHomeUiState.DisplayPassengerPlaces -> {
                    logD("setting places: display passengers places")
                    displayPlaces(state)
                }
                is PassengerHomeUiState.Arrived -> TODO()
                is PassengerHomeUiState.DisplayDriverOffer -> TODO()

                is PassengerHomeUiState.EnRoute -> TODO()
                PassengerHomeUiState.Error -> TODO()
                PassengerHomeUiState.Loading -> TODO()
                is PassengerHomeUiState.PassengerPickUp -> TODO()

                is PassengerHomeUiState.WaitForDriverAcceptance -> TODO()
            }
        }
    }

    private fun displayPlaces(state: PassengerHomeUiState.DisplayPassengerPlaces) {
        hideAllViews()
        map.clear()
        drawLine(state.pickupLatLng, state.destinationLatLng)

    }

    private fun setNavigationDrawer() {
        header = binding.navigationView.getHeaderView(0)
        binding.navigationView.getHeaderView(0)

        binding.navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_item_profile -> {
                    val action = PassengerHomeFragmentDirections.actionHomeFragmentToProfileFragment2()
                    findNavController().navigate(action)
                }

                R.id.nav_item_wallet -> {
                    val action = PassengerHomeFragmentDirections.actionHomeFragmentToWalletFragment2()
                    findNavController().navigate(action)
                }

                R.id.nav_item_payment -> {
                    val action = PassengerHomeFragmentDirections.actionHomeFragmentToPaymentFragment2()
                    findNavController().navigate(action)
                }

                R.id.nav_item_rides -> {
                    val action = PassengerHomeFragmentDirections.actionHomeFragmentToRidesFragment()
                    findNavController().navigate(action)
                }

                R.id.nav_item_help -> {
                    val action = PassengerHomeFragmentDirections.actionHomeFragmentToHelpFragment2()
                    findNavController().navigate(action)
                }

                R.id.nav_item_Settings -> {
                    //val action = HomeFragmentDirections.actionHomeFragmentToSettingsFragment2()
                    val action = PassengerHomeFragmentDirections.actionHomeFragmentToSetFragment()
                    findNavController().navigate(action)
                }
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
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

        val riyadh = LatLng(24.7136, 46.6753)

//        val saudiArabiaBounds = LatLngBounds(
//            LatLng(16.0, 34.0), // Southwest corner
//            LatLng(33.0, 56.0)  // Northeast corner
//        )
//
//        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())
//        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
//            if (location != null) {
//                // Animate the camera to the user's current location
//                currentLocation = LatLng(location.latitude, location.longitude)
//                map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f))
//            } else {
//                // If the user's location is not available, animate the camera to Riyadh
//                map.animateCamera(CameraUpdateFactory.newLatLngZoom(riyadh, 15f))
//            }
//        }
//
//        map.addMarker(MarkerOptions().position(riyadh).title("Riyadh"))
//        map.animateCamera(CameraUpdateFactory.newLatLng(riyadh))
//        map.isMyLocationEnabled = true
//
//        // place my-location button on bottom-right-corner
//        val locationButton =
//            (this.view?.findViewById<View>(Integer.parseInt("1"))?.parent as View).findViewById<View>(
//                Integer.parseInt("2")
//            )
//        val rlp = locationButton.getLayoutParams() as RelativeLayout.LayoutParams
//        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0)
//        rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE)
//        rlp.setMargins(0, 0, 30, 30)
//
////        map.setPadding(0, 1000, 0, 0)
//        map.uiSettings.apply {
//            isZoomControlsEnabled = true
//            isZoomGesturesEnabled = true
//            isRotateGesturesEnabled = true
//            isTiltGesturesEnabled = false
//            isCompassEnabled = false
//            isScrollGesturesEnabled = true
//            isMyLocationButtonEnabled = true
//        }
//        map.setLatLngBoundsForCameraTarget(saudiArabiaBounds)
//        map.setMinZoomPreference(8f)
//        map.setMaxZoomPreference(15f)
//        map.setOnMyLocationButtonClickListener(this)
//
//
////        val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())
////        val lat = fusedLocationProviderClient.lastLocation.getResult().latitude
////        val lng = fusedLocationProviderClient.lastLocation.getResult().latitude
////        map.addMarker(MarkerOptions().position(LatLng(lat, lng)))



//        Log.d(TAG, viewModel.pickup.toString())
//        if (viewModel.pickup != null && viewModel.destination != null) {
//            val pickupLatLng = getLatLngFromAddress(viewModel.pickup!!)
//            val destinationLatLng = getLatLngFromAddress(viewModel.destination!!)
//            drawLineOnMap(pickupLatLng, destinationLatLng)
//        }

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

                    //viewModel.updateLocation(lastLocation.toLatLng())
                    val newLocation  = lastLocation.toLatLng()

                    viewModel.setPassengerCurrentLocation(newLocation, getAddressFromLatLng(newLocation))
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
                    directions.await()
                }


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


    private fun getLatLngFromAddress(address: String): LatLng {
        try {
            val geocoder = Geocoder(requireContext())
            val results = geocoder.getFromLocationName(address, 1)
            val location = results?.get(0)!!
            return LatLng(location.latitude, location.longitude)
        } catch (e: Exception) {
            toast("an error has occurred in getLatLngFromAddress: ${e.message}")
        }
        return LatLng(.0, .0)
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
        return false
    }

    private fun loadImage(uri: String) {
//        Glide.with(requireContext())
//            .load(uri)
//            .diskCacheStrategy(DiskCacheStrategy.NONE)
//            .skipMemoryCache(true)
//            .into(header.findViewById(R.id.profile_image))
        TODO("load image;")
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            SettingsDialog.Builder(requireActivity()).build().show()
        } else {
            requestLocationPermission(this)
        }

    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
    }

    private fun hideAllViews() {
        binding.apply {
//            pathDetailsView.pathDetailsLayout.hide()
//            ridesRequestView.ridesRequestLayout.hide()
//            waitingPassengerView.waitingPassengerLayout.hide()
//            passengerPickupView.passengerPickupLayout.hide()
//            enRouteView.enRouteLayout.hide()
//            passengerArrivedView.passengerArrivedLayout.hide()
//            continueRideView.continueRideLayout.hide()
            pickupIl.hide()
        }
    }
}