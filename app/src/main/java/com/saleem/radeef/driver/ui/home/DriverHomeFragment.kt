package com.saleem.radeef.driver.ui.home

import android.annotation.SuppressLint
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RelativeLayout
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
import com.saleem.radeef.ui.map.HomeFragmentDirections
import com.saleem.radeef.ui.map.TAG
import com.saleem.radeef.util.toast
import com.vmadalin.easypermissions.EasyPermissions
import configureMapSettings
import RIYADH
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.saleem.radeef.ui.drawer.payment.DriverPaymentFragmentDirections
import com.saleem.radeef.util.UiState
import com.saleem.radeef.util.enable
import com.saleem.radeef.util.hide
import com.saleem.radeef.util.logD
import com.saleem.radeef.util.show
import configureLocationButton
import dagger.hilt.android.AndroidEntryPoint
import saudiArabiaBounds
import setCameraBoundsAndZoom
import java.lang.Exception

@AndroidEntryPoint
class DriverHomeFragment : Fragment(R.layout.driver_fragment_home), OnMapReadyCallback,
    EasyPermissions.PermissionCallbacks, GoogleMap.OnMyLocationButtonClickListener {
    private lateinit var map: GoogleMap
    lateinit var binding: DriverFragmentHomeBinding
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var currentLocation: LatLng
    val viewModel: DriverMapViewModel by activityViewModels()
    private lateinit var polyline: Polyline

    lateinit var header: View


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
                    val action = DriverHomeFragmentDirections.actionDriverHomeFragmentToDriverProfileFragment()
                    findNavController().navigate(action)
                }

                R.id.nav_item_wallet -> {
                    val action = DriverHomeFragmentDirections.actionDriverHomeFragmentToDriverWalletFragment()
                    findNavController().navigate(action)
                }

                R.id.nav_item_payment -> {
                    val action = DriverHomeFragmentDirections.actionDriverHomeFragmentToDriverPaymentFragment()
                    findNavController().navigate(action)

                }

                R.id.nav_item_rides -> {
                    val action = DriverHomeFragmentDirections.actionDriverHomeFragmentToDriverRidesFragment()
                    findNavController().navigate(action)
                }

                R.id.nav_item_help -> {
                    val action = DriverHomeFragmentDirections.actionDriverHomeFragmentToDriverHelpFragment()
                    findNavController().navigate(action)
                }

                R.id.nav_item_Settings -> {
                    //val action = HomeFragmentDirections.actionHomeFragmentToSettingsFragment2()
                    val action = DriverHomeFragmentDirections.actionDriverHomeFragmentToDriverSettingsFragment()
                    findNavController().navigate(action)
                }
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            true
        }


        binding.pickupEt.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                TODO("create a fragment to set pickup and destination")
//                val action =
//                    HomeFragmentDirections.actionHomeFragmentToSearchFragment(currentLocation)
//                findNavController().navigate(action)
            }
        }


        viewModel.driver.observe(viewLifecycleOwner) {state ->
            when (state) {
                UiState.Loading -> {
//                    binding.progressBar.show()
//                    binding.continueBt.setText("")
                }
                is UiState.Success -> {
                    // binding.progressBar.hide()
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
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap


        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())
        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                // Animate the camera to the user's current location
                currentLocation = LatLng(location.latitude, location.longitude)
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
            val pickupLatLng = getLatLngFromAddress(viewModel.pickup!!)
            val destinationLatLng = getLatLngFromAddress(viewModel.destination!!)
            drawLineOnMap(pickupLatLng, destinationLatLng)
        }

    }

    private fun drawLineOnMap(pickupLatLng: LatLng, destinationLatLng: LatLng) {
        val context = GeoApiContext.Builder()
            .apiKey(getString(R.string.google_maps_key))
            .build()

        Log.d(TAG, context.toString())
        Log.d(TAG, pickupLatLng.toString())
        Log.d(TAG, destinationLatLng.toString())
        try {
            Log.d(TAG, "before directions init")
            val directions = DirectionsApi.newRequest(context)
            Log.d(TAG, directions.toString())
            directions.origin(
                com.google.maps.model.LatLng(
                    pickupLatLng.latitude,
                    pickupLatLng.longitude
                )
            )
            Log.d(TAG, "line: 213: ${directions.toString()}")
            directions
                .destination(
                    com.google.maps.model.LatLng(
                        destinationLatLng.latitude,
                        destinationLatLng.longitude
                    )
                )
            Log.d(TAG, "line: 221: ${directions.toString()}")
            directions
                .mode(TravelMode.DRIVING)
                .units(Unit.METRIC)


            val result = directions.await()
            val dist = result.routes[0].legs.sumOf { it.distance.inMeters } / 1000.0
            toast(dist.toString())

            val route = result.routes[0]

            // Get the polyline data from the route
            val encodedPolyline = route.overviewPolyline.encodedPath

// Decode the polyline data into a list of LatLng objects
            val decodedPolyline = PolyUtil.decode(encodedPolyline)

            val polylineOptions = PolylineOptions()
                .addAll(decodedPolyline)
                .color(R.color.md_theme_light_primary)
                .width(10f)

// Add the polyline to the map
            val polyline = map.addPolyline(polylineOptions)

// Move the camera to the bounding box of the polyline
            val boundsBuilder = LatLngBounds.Builder()
            for (point in decodedPolyline) {
                boundsBuilder.include(point)
            }
            val bounds = boundsBuilder.build()
            val cameraPosition = CameraPosition.Builder()
                .zoom(15.0f) //  desired zoom level as a float value
                .bearing(45.0f) //  desired bearing angle as a float value
                .tilt(30.0f) //  desired tilt angle as a float value
                .build()

            val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 100).let {
                CameraUpdateFactory.newCameraPosition(cameraPosition)
            }
            map.animateCamera(cameraUpdate)

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