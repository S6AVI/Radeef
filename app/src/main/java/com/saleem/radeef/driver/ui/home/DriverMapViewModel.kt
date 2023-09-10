package com.saleem.radeef.driver.ui.home

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.SupportMapFragment
import com.saleem.radeef.R
import com.saleem.radeef.util.Permissions
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog

class DriverMapViewModel: ViewModel() {

    var pickup: String? = null
    var destination: String? = null



    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray, fragment: DriverHomeFragment) {
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, fragment)
    }

    fun onPermissionsDenied(fragment: Fragment, requestCode: Int, perms: List<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(fragment, perms)) {
            SettingsDialog.Builder(fragment.requireActivity()).build().show()
        } else {
            Permissions.requestLocationPermission(fragment)
        }
    }

    fun onPermissionsGranted(fragment: DriverHomeFragment, requestCode: Int, perms: List<String>) {
        // Handle permissions granted here
        val mapFragment = fragment.childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(fragment)
    }


}