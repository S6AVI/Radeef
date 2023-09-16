package com.saleem.radeef.driver.ui.home

import androidx.fragment.app.Fragment
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.SupportMapFragment
import com.saleem.radeef.R
import com.saleem.radeef.data.firestore.driver.Driver
import com.saleem.radeef.driver.repo.DriverRepository
import com.saleem.radeef.util.Permissions
import com.saleem.radeef.util.UiState
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog

class DriverMapViewModel @ViewModelInject constructor(
    val repository: DriverRepository
): ViewModel() {

    var pickup: String? = null
    var destination: String? = null

    private val _driver = MutableLiveData<UiState<Driver>>()
    val driver: LiveData<UiState<Driver>>
        get() = _driver


    private fun getDriver() {
        _driver.value = UiState.Loading
        repository.getDriver {
            _driver.value = it
        }
    }

    init {
        getDriver()
    }

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