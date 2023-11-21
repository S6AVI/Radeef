package com.saleem.radeef.passenger.ui.map

import android.annotation.SuppressLint
import android.location.Geocoder
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.RelativeLayout
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.MAP_TYPE_HYBRID
import com.google.android.gms.maps.GoogleMap.MAP_TYPE_SATELLITE
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.DirectionsApi
import com.google.maps.DirectionsApiRequest
import com.google.maps.GeoApiContext
import com.google.maps.android.PolyUtil
import com.google.maps.errors.ApiException
import com.google.maps.model.TravelMode
import com.google.maps.model.Unit
import com.saleem.radeef.R
import com.saleem.radeef.databinding.FragmentHomeBinding
import com.saleem.radeef.util.Permissions.hasLocationPermission
import com.saleem.radeef.util.Permissions.requestLocationPermission
import com.saleem.radeef.util.toast
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog
import java.lang.Exception
import java.lang.reflect.InvocationTargetException


class HomeFragment : Fragment(R.layout.fragment_home), OnMapReadyCallback,
    EasyPermissions.PermissionCallbacks, GoogleMap.OnMyLocationButtonClickListener {
    private lateinit var map: GoogleMap
    lateinit var binding: FragmentHomeBinding
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var currentLocation: LatLng
    val viewModel: MapViewModel by activityViewModels()
    private lateinit var polyline: Polyline

//    lateinit var autoCompleteFragment: AutocompleteSupportFragment

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentHomeBinding.bind(view)

        if (hasLocationPermission(requireContext())) {
            val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
            mapFragment?.getMapAsync(this)
        } else {
            requestLocationPermission(this)
        }


        binding.menuButton.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
//            val action = HomeFragmentDirections.actionHomeFragmentToNavigationDrawerFragment()
//            findNavController().navigate(action)
        }


        binding.navigationView.getHeaderView(0)

        binding.navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_item_profile -> {
                    val action = HomeFragmentDirections.actionHomeFragmentToProfileFragment2()
                    findNavController().navigate(action)
                }

                R.id.nav_item_wallet -> {
                    val action = HomeFragmentDirections.actionHomeFragmentToWalletFragment2()
                    findNavController().navigate(action)
                }

                R.id.nav_item_payment -> {
                    val action = HomeFragmentDirections.actionHomeFragmentToPaymentFragment2()
                    findNavController().navigate(action)
                }

                R.id.nav_item_rides -> {
                    val action = HomeFragmentDirections.actionHomeFragmentToRidesFragment()
                    findNavController().navigate(action)
                }

                R.id.nav_item_help -> {
                    val action = HomeFragmentDirections.actionHomeFragmentToHelpFragment2()
                    findNavController().navigate(action)
                }

                R.id.nav_item_Settings -> {
                    //val action = HomeFragmentDirections.actionHomeFragmentToSettingsFragment2()
                    val action = HomeFragmentDirections.actionHomeFragmentToSetFragment()
                    findNavController().navigate(action)
                }
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            true
        }


//        binding.pickupIl.setOnClickListener {
//            val action = HomeFragmentDirections.actionHomeFragmentToSearchFragment()
//            findNavController().navigate(action)
//        }

        binding.pickupEt.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                val action =
                    HomeFragmentDirections.actionHomeFragmentToSearchFragment(currentLocation)
                findNavController().navigate(action)
            }
        }

    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap


        val riyadh = LatLng(24.7136, 46.6753)

        val saudiArabiaBounds = LatLngBounds(
            LatLng(16.0, 34.0), // Southwest corner
            LatLng(33.0, 56.0)  // Northeast corner
        )

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                // Animate the camera to the user's current location
                currentLocation = LatLng(location.latitude, location.longitude)
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f))
            } else {
                // If the user's location is not available, animate the camera to Riyadh
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(riyadh, 15f))
            }
        }

        map.addMarker(MarkerOptions().position(riyadh).title("Riyadh"))
        map.animateCamera(CameraUpdateFactory.newLatLng(riyadh))
        map.isMyLocationEnabled = true

        // place my-location button on bottom-right-corner
        val locationButton =
            (this.view?.findViewById<View>(Integer.parseInt("1"))?.parent as View).findViewById<View>(
                Integer.parseInt("2")
            )
        val rlp = locationButton.getLayoutParams() as RelativeLayout.LayoutParams
        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0)
        rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE)
        rlp.setMargins(0, 0, 30, 30)

//        map.setPadding(0, 1000, 0, 0)
        map.uiSettings.apply {
            isZoomControlsEnabled = true
            isZoomGesturesEnabled = true
            isRotateGesturesEnabled = true
            isTiltGesturesEnabled = false
            isCompassEnabled = false
            isScrollGesturesEnabled = true
            isMyLocationButtonEnabled = true
        }
        map.setLatLngBoundsForCameraTarget(saudiArabiaBounds)
        map.setMinZoomPreference(8f)
        map.setMaxZoomPreference(15f)
        map.setOnMyLocationButtonClickListener(this)


//        val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())
//        val lat = fusedLocationProviderClient.lastLocation.getResult().latitude
//        val lng = fusedLocationProviderClient.lastLocation.getResult().latitude
//        map.addMarker(MarkerOptions().position(LatLng(lat, lng)))



        Log.d(TAG, viewModel.pickup.toString())
        if (viewModel.pickup != null && viewModel.destination != null) {
            val pickupLatLng = getLatLngFromAddress(viewModel.pickup!!)
            val destinationLatLng = getLatLngFromAddress(viewModel.destination!!)
            drawLineOnMap(pickupLatLng, destinationLatLng)
        }

//        polyline = map.addPolyline(
//            PolylineOptions()
//                .color(R.color.md_theme_light_primary)
//                .width(10f)
//                .geodesic(true)
//        )

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
//            val points = mutableListOf<LatLng>()
//            for (step in result.routes[0].legs[0].steps) {
//                step.polyline.decodePath()?.forEach {
//                    points.add(LatLng(it.lat, it.lng))
//                }
//            }
//            val polylineOptions = PolylineOptions()
//                .color(R.color.md_theme_light_primary)
//                .width(15f)
//                .geodesic(true)
//                .addAll(points)
//
//            val polyline = map.addPolyline(polylineOptions)

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
                .zoom(15.0f) // your desired zoom level as a float value
                .bearing(45.0f) // your desired bearing angle as a float value
                .tilt(30.0f) // your desired tilt angle as a float value
                .build()


            //map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))

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
}