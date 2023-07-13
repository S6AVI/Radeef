package com.saleem.radeef.ui.map

import android.annotation.SuppressLint
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.view.View
import android.widget.RelativeLayout
import androidx.core.view.GravityCompat
import androidx.navigation.fragment.findNavController

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.saleem.radeef.R
import com.saleem.radeef.databinding.FragmentHomeBinding
import com.saleem.radeef.util.Permissions.hasLocationPermission
import com.saleem.radeef.util.Permissions.requestLocationPermission
import com.saleem.radeef.util.toast
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog

class HomeFragment : Fragment(R.layout.fragment_home), OnMapReadyCallback,
    EasyPermissions.PermissionCallbacks, GoogleMap.OnMyLocationButtonClickListener {
    private lateinit var map: GoogleMap
    lateinit var binding: FragmentHomeBinding

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        val riyadh = LatLng(24.7136, 46.6753)

        val saudiArabiaBounds = LatLngBounds(
            LatLng(16.0, 34.0), // Southwest corner
            LatLng(33.0, 56.0)  // Northeast corner
        )

        map.addMarker(MarkerOptions().position(riyadh).title("Riyadh"))
        map.moveCamera(CameraUpdateFactory.newLatLng(riyadh))
        map.isMyLocationEnabled = true

        // place my-location button on bottom-right-corner
        val locationButton = (this.view?.findViewById<View>(Integer.parseInt("1"))?.parent as View).findViewById<View>(Integer.parseInt("2"))
        val rlp =  locationButton.getLayoutParams() as RelativeLayout.LayoutParams
        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0)
        rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE)
        rlp.setMargins(0, 0, 30, 30)

//        map.setPadding(0, 1000, 0, 0)
        map.uiSettings.apply {
            isZoomControlsEnabled = false
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
    }

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

        binding.navigationView.setNavigationItemSelectedListener { menuItem ->
            when(menuItem.itemId) {
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
                    val action = HomeFragmentDirections.actionHomeFragmentToSettingsFragment2()
                    findNavController().navigate(action)
                }
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            true
        }





    }

    override fun onMyLocationButtonClick(): Boolean {
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