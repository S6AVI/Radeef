package com.saleem.radeef.passenger.ui.home

import android.annotation.SuppressLint
import android.content.Intent
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory
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
import com.saleem.radeef.databinding.FragmentHomeBinding
import com.saleem.radeef.passenger.ui.PassengerHomeUiState
import com.saleem.radeef.util.MIN_UPDATE_DISTANCE_METERS
import com.saleem.radeef.util.Permissions.hasLocationPermission
import com.saleem.radeef.util.Permissions.requestLocationPermission
import com.saleem.radeef.util.UiState
import com.saleem.radeef.util.formatCost
import com.saleem.radeef.util.formatDistance
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


class PassengerHomeFragment : Fragment(R.layout.fragment_home), OnMapReadyCallback,
    EasyPermissions.PermissionCallbacks, GoogleMap.OnMyLocationButtonClickListener {
    private lateinit var map: GoogleMap
    lateinit var binding: FragmentHomeBinding
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    val viewModel: PassengerHomeViewModel by activityViewModels()
    private lateinit var header: View

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
                }

                is UiState.Failure -> {
                    toast(state.error.toString())
                }
            }
        }

        observeCurrentHomeState()


        binding.pickupEt.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                val action =
                    PassengerHomeFragmentDirections.actionHomeFragmentToSearchFragment()
                findNavController().navigate(action)
            }
        }
    }

    private fun observeCurrentHomeState() {

        viewModel.currentHomeState.observe(viewLifecycleOwner) { state ->

            when (state) {

                PassengerHomeUiState.SettingPlaces -> {
                    hideAllViews()
                    binding.pickupIl.show()
                }

                is PassengerHomeUiState.DisplayPassengerPlaces -> {
                    displayPlaces(state)
                }

                is PassengerHomeUiState.WaitForDriverAcceptance -> {
                    waitDriverAcceptance(state)
                }

                is PassengerHomeUiState.DisplayDriverOffer -> {
                    displayDriverOffer(state)
                }

                is PassengerHomeUiState.EnRoute -> {
                    enRoute(state)
                }

                is PassengerHomeUiState.Arrived -> {
                    arrived(state)
                }

                PassengerHomeUiState.Error -> {}
                PassengerHomeUiState.Loading -> {
                    hideAllViews()
                    binding.loadingView.viewLoadingLayout.show()
                }

                is PassengerHomeUiState.PassengerPickUp -> {
                    passengerPickup(state)
                }
            }
        }
    }

    /*
        state handlers
     */
    private fun arrived(state: PassengerHomeUiState.Arrived) {
        hideAllViews()
        map.clear()
        val ride = state.ride
        val driver = state.driver

        binding.passengerArrivedView.apply {
            loadImage(driver.personalPhotoUrl, driverImageView)
            driverNameTextView.text = driver.name
            costTextView.text = ride.chargeAmount.formatCost()
            passengerArrivedLayout.show()
        }

        binding.passengerArrivedView.doneButton.setOnClickListener {
            viewModel.onDoneButtonClicked()
        }
    }

    private fun enRoute(state: PassengerHomeUiState.EnRoute) {
        hideAllViews()
        map.clear()
        val ride = state.ride
        val driver = state.driver
        val vehicle = state.vehicle
        setMarker(driver.pickupLatLng)
        drawLine(driver.pickupLatLng, ride.passengerDestLatLng)
        binding.enRouteView.apply {
            loadImage(driver.personalPhotoUrl, driverImageView)
            driverNameTextView.text = driver.name
            carTextView.text = getString(R.string.vehicle_make_model, vehicle.make, vehicle.model)
            plateTextView.text = vehicle.plateNumber
            destTextView.text = getAddressFromLatLng(ride.passengerDestLatLng)
            distanceTextView.text = state.distance.formatDistance()
            enRouteLayout.show()
        }

        binding.enRouteView.callButton.setOnClickListener {
            makePhoneCall(driver.phoneNumber)
        }
    }

    private fun passengerPickup(state: PassengerHomeUiState.PassengerPickUp) {
        hideAllViews()
        map.clear()
        val ride = state.ride
        val driver = state.driver
        val vehicle = state.vehicle
        setMarker(driver.pickupLatLng)
        drawLine(ride.passengerPickupLatLng, ride.passengerDestLatLng)
        drawLine(driver.pickupLatLng, ride.passengerPickupLatLng, R.color.md_theme_light_secondary)

        binding.passengerPickupView.apply {
            loadImage(driver.personalPhotoUrl, driverImageView)
            driverNameTextView.text = driver.name
            carTextView.text = getString(R.string.vehicle_make_model, vehicle.make, vehicle.model)
            plateTextView.text = vehicle.plateNumber
            pickupTextView.text = getAddressFromLatLng(ride.passengerPickupLatLng)
            distanceTextView.text = state.distance.formatDistance()
            passengerPickupLayout.show()
        }

        binding.passengerPickupView.callButton.setOnClickListener {
            makePhoneCall(driver.phoneNumber)
        }

        binding.passengerPickupView.cancelButton.setOnClickListener {
            viewModel.onCancelButtonClicked(ride)
        }

    }



    private fun displayDriverOffer(state: PassengerHomeUiState.DisplayDriverOffer) {
        binding.loadingView.viewLoadingLayout.show()
        val ride = state.ride
        val driver = state.driver
        val vehicle = state.vehicle
        drawLine(ride.passengerPickupLatLng, ride.passengerDestLatLng)
        binding.displayOfferView.apply {
            loadImage(driver.personalPhotoUrl, driverImageView)
            hideButton.text = getString(R.string.cancel_label)
            acceptButton.text = getString(R.string.confrim_lable)
            nameTextView.text = driver.name
            carTextView.text = getString(R.string.vehicle_make_model, vehicle.make, vehicle.model)
            plateTextView.text = vehicle.plateNumber
            pickupTextView.text = getAddressFromLatLng(ride.passengerPickupLatLng)
            destinationTextView.text = getAddressFromLatLng(ride.passengerDestLatLng)
            distanceTextView.text = state.distance.formatDistance()
            costTextView.text = ride.chargeAmount.formatCost()
            hideAllViews()
            displayOfferLayout.show()
        }

        binding.displayOfferView.acceptButton.setOnClickListener {
            viewModel.onConfirmButtonClicked(ride)
        }
        binding.displayOfferView.hideButton.setOnClickListener {
            viewModel.onCancelButtonClicked(ride)

        }
    }

    private fun waitDriverAcceptance(state: PassengerHomeUiState.WaitForDriverAcceptance) {
        hideAllViews()
        val ride = state.ride
        drawLine(ride.passengerPickupLatLng, ride.passengerDestLatLng)
        binding.passengerWaitView.apply {
            pickupTextView.text = getAddressFromLatLng(ride.passengerPickupLatLng)
            destinationTextView.text = getAddressFromLatLng(ride.passengerDestLatLng)
            distanceTextView.text = state.distance.formatDistance()
            passengerWaitLayout.show()
        }

        binding.passengerWaitView.cancelButton.setOnClickListener {
            viewModel.onCancelButtonClicked(ride)
        }
    }

    private fun displayPlaces(state: PassengerHomeUiState.DisplayPassengerPlaces) {
        hideAllViews()
        map.clear()
        drawLine(state.pickupLatLng, state.destinationLatLng)

        binding.pathDetailsView.apply {
            pickupTitleTextView.text = getAddressFromLatLng(state.pickupLatLng)
            destinationTitleTextView.text = getAddressFromLatLng(state.destinationLatLng)
            distanceTextView.text = state.distance.formatDistance()
        }
        binding.pathDetailsView.pathDetailsLayout.show()

        binding.pathDetailsView.changeDestinationTextView.text = getString(R.string.change_places)
        binding.pathDetailsView.changeDestinationTextView.setOnClickListener {
            val action =
                PassengerHomeFragmentDirections.actionHomeFragmentToSearchFragment()
            findNavController().navigate(action)
        }

        binding.pathDetailsView.searchButton.setOnClickListener {
            viewModel.onSearchButtonClicked(state)
        }
    }

    private fun setNavigationDrawer() {
        header = binding.navigationView.getHeaderView(0)
        binding.navigationView.getHeaderView(0)

        binding.navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_item_profile -> {
                    val action =
                        PassengerHomeFragmentDirections.actionHomeFragmentToProfileFragment2()
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


            }
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
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

                    val newLocation = lastLocation.toLatLng()

                    viewModel.setPassengerCurrentLocation(
                        newLocation,
                        getAddressFromLatLng(newLocation)
                    )
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(newLocation, 14f))
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

                val encodedPolyline = route.overviewPolyline.encodedPath

                val decodedPolyline = PolyUtil.decode(encodedPolyline)

                val boundsBuilder = LatLngBounds.Builder()
                for (point in decodedPolyline) {
                    boundsBuilder.include(point)
                }

                val polylineOptions = PolylineOptions()
                    .addAll(decodedPolyline)
                    .color(lineColor)
                    .width(10f)


                val endMarkerOptions = MarkerOptions()
                    .position(decodedPolyline.last())
                    .title("End")
                map.addMarker(endMarkerOptions)

            } catch (e: Exception) {
                logD(e.toString())
            }
        }
    }


    private fun getAddressFromLatLng(latlng: LatLng): String {
        return try {
            val geocoder = Geocoder(requireContext(), Locale.getDefault())
            val addresses = geocoder.getFromLocation(latlng.latitude, latlng.longitude, 1)
            addresses?.firstOrNull()?.featureName ?: ""
        } catch (e: Exception) {
            logD("get address error: ${e.message}")
            ""
        }
    }


    override fun onMyLocationButtonClick(): Boolean {
        return false
    }

    private fun loadImage(uri: String, view: ImageView) {
        Glide.with(requireContext())
            .load(uri)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .into(view)
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

    private fun makePhoneCall(phoneNumber: String) {
        val intent = Intent(Intent.ACTION_DIAL)
        intent.data = Uri.parse("tel:$phoneNumber")
        startActivity(intent)
    }

    private fun hideAllViews() {
        binding.apply {
            pathDetailsView.pathDetailsLayout.hide()
            passengerWaitView.passengerWaitLayout.hide()
            displayOfferView.displayOfferLayout.hide()
            passengerPickupView.passengerPickupLayout.hide()
            enRouteView.enRouteLayout.hide()
            passengerArrivedView.passengerArrivedLayout.hide()
            loadingView.viewLoadingLayout.hide()
            pickupIl.hide()
        }
    }

    private fun setMarker(pickupLatLng: LatLng) {
        map.addMarker(
            MarkerOptions().apply {
                position(pickupLatLng)
                val markerBitmap =
                    requireContext().getDrawable(R.drawable.ic_car_marker)
                        ?.toBitmap()
                markerBitmap?.let {
                    icon(BitmapDescriptorFactory.fromBitmap(it))
                }
            })
    }
}