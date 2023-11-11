package com.saleem.radeef.driver.ui.home

import androidx.fragment.app.Fragment
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.SupportMapFragment
import com.saleem.radeef.R
import com.saleem.radeef.data.RadeefLocation
import com.saleem.radeef.data.firestore.driver.Driver
import com.saleem.radeef.driver.repo.DriverRepository
import com.saleem.radeef.util.Permissions
import com.saleem.radeef.util.UiState
import com.saleem.radeef.util.logD
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class DriverMapViewModel @ViewModelInject constructor(
    val repository: DriverRepository
) : ViewModel() {

    var pickup: RadeefLocation? = null
    var destination: RadeefLocation? = null

    private val _driver = MutableLiveData<UiState<Driver>>()
    val driver: LiveData<UiState<Driver>>
        get() = _driver

    var driverData: Driver? = null

//    private val _updateResult = MutableLiveData<UiState<Boolean>>()
//    val updateResult: LiveData<UiState<Boolean>>
//        get() = _updateResult

    private val updateResultChannel = Channel<UiState<Boolean>>()
    val updateResult = updateResultChannel.receiveAsFlow()

    private fun getDriver() {
        _driver.value = UiState.Loading
        repository.getDriver {state ->
            _driver.value = state

            if (state is UiState.Success) {
                logD("MapViewModel: in getDriver: success: ${state.data}")
                driverData = state.data
            } else {
                logD("some problem in getDriver")
            }
        }
    }

    init {
        logD("init is called")
        getDriver()
    }

    fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
        fragment: DriverHomeFragment
    ) {
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
        val mapFragment =
            fragment.childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(fragment)
    }


    fun updateDriverLocations() {
        viewModelScope.launch {
            updateResultChannel.send(UiState.Loading)
            logD("pickup: ${pickup?.title}\ndestination: ${destination?.title}")
            if (pickup != null && destination != null) {

                repository.updateDriverLocations(pickup!!, destination!!) { result ->
                    logD("viewModel: update locations - 78: $result")
                    viewModelScope.launch {
                    updateResultChannel.send(result)
                    }
                }
            } else {
                logD("something is null")
            }
        }
    }

    fun getDriverLocations() {

    }


}