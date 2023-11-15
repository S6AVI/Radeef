package com.saleem.radeef.driver.ui.home

import androidx.fragment.app.Fragment
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.SupportMapFragment
import com.google.firebase.firestore.auth.User
import com.saleem.radeef.R
import com.saleem.radeef.data.RadeefLocation
import com.saleem.radeef.data.firestore.driver.Driver
import com.saleem.radeef.data.firestore.driver.UserStatus
import com.saleem.radeef.data.repository.AuthRepository
import com.saleem.radeef.data.repository.CloudRepository
import com.saleem.radeef.driver.DriverHomeUiState
import com.saleem.radeef.driver.repo.DriverRepository
import com.saleem.radeef.util.Permissions
import com.saleem.radeef.util.UiState
import com.saleem.radeef.util.logD
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class DriverHomeViewModel @ViewModelInject constructor(
    val repository: DriverRepository,
    val passengerRepo: CloudRepository
) : ViewModel() {

    var pickup: RadeefLocation? = null
    var destination: RadeefLocation? = null

    private val _driver = MutableLiveData<UiState<Driver>>()
    val driver: LiveData<UiState<Driver>>
        get() = _driver

    var driverData: Driver? = null

    private val _currentHomeState = MutableLiveData<DriverHomeUiState>()
    val currentHomeState: LiveData<DriverHomeUiState>
        get() = _currentHomeState

    // Method to update the current state
    private fun updateDriverState(newState: DriverHomeUiState) {
        _currentHomeState.value = newState
    }

//    private val _updateResult = MutableLiveData<UiState<Boolean>>()
//    val updateResult: LiveData<UiState<Boolean>>
//        get() = _updateResult

//    private val updateResultChannel = Channel<HomeEvent>()
//    val updateResult = updateResultChannel.receiveAsFlow()

//    private val startSearchingChannel = Channel<HomeEvent>()
//    val startSearching = startSearchingChannel.receiveAsFlow()

    private val homeEventChannel = Channel<HomeEvent>()
    val homeEvent = homeEventChannel.receiveAsFlow()


//    private fun mapToUiState(driverUiState: DriverHomeUiState): UiState<DriverHomeUiState> {
//        return when (driverUiState) {
//            is DriverHomeUiState.SettingPlaces -> UiState.Success(driverUiState)
//            // Map other driver states to UiState similarly
//            else -> {
//                UiState.Loading
//            }
//        }
//    }

    private fun setHomeUiState(status: String) {
        logD("in home ui state setter - first line")

        val data = driverData!!
        when (status) {
            UserStatus.INACTIVE.value -> {
                if (data.pickup.latitude == 0.0) {
                    _currentHomeState.value = DriverHomeUiState.SettingPlaces
                } else {
                    _currentHomeState.value = DriverHomeUiState.DisplayDriverPlaces(
                        driverLatLng = data.pickupLatLng,
                        driverAddress = data.pickup_title,
                        driverDestinationLatLng = data.destinationLatLng,
                        driverDestinationAddress = data.destination_title,
                    )
                }
            }

            UserStatus.SEARCHING.value -> {
                _currentHomeState.value = DriverHomeUiState.SearchingForPassengers
            }

        }
        //_currentHomeState.value = DriverHomeUiState.SettingPlaces
    }

    private fun getDriver() {
        _driver.value = UiState.Loading
        repository.getDriver { state ->

            if (state is UiState.Success) {
                logD("MapViewModel: in getDriver: success: ${state.data}")
                driverData = state.data
                setHomeUiState(driverData!!.status)
                //_driver.value = UiState.Success(state.data)

            } else {
                logD("some problem in getDriver")
            }
            _driver.value = state


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


//    fun updateDriverLocations() {
//        viewModelScope.launch {
//            //updateResultChannel.send(UiState.Loading)
//            updateResultChannel.send(HomeEvent.UpdateResult(UiState.Loading))
//            logD("pickup: ${pickup?.title}\ndestination: ${destination?.title}")
//            if (pickup != null && destination != null) {
//
//                repository.updateDriverLocations(pickup!!, destination!!) { result ->
//                    logD("viewModel: update locations - 78: $result")
//                    viewModelScope.launch {
//                        updateResultChannel.send(result)
//                    }
//                }
//            } else {
//                logD("something is null")
//            }
//        }
//    }

    fun updateDriverLocations() {
        viewModelScope.launch {
            //updateResultChannel.send(UiState.Loading)
            homeEventChannel.send(HomeEvent.UpdateResult(UiState.Loading))
            logD("pickup: ${pickup?.title}\ndestination: ${destination?.title}")
            if (pickup != null && destination != null) {

                repository.updateDriverLocations(pickup!!, destination!!) { result ->
                    logD("viewModel: update locations - 78: $result")
                    viewModelScope.launch {
                        homeEventChannel.send(HomeEvent.UpdateResult(result))
                    }
                }
            } else {
                logD("something is null")
            }
        }
    }

    fun onSearchButtonClicked() {

        logD("onSearchButtonClicked start")
        _currentHomeState.value = DriverHomeUiState.Loading
        viewModelScope.launch {
            //homeEventChannel.send(HomeEvent.StartSearching(UiState.Loading))

            //driverData = driverData!!.copy(status = UserStatus.SEARCHING.value)
            repository.updateDriver(
                driverData!!.copy(status = UserStatus.SEARCHING.value)
            ) { result ->
                if (result is UiState.Success) {
                    driverData = driverData!!.copy(status = UserStatus.SEARCHING.value)
                    _currentHomeState.value = DriverHomeUiState.SearchingForPassengers
                } else {
                    logD("error")
                    //_currentHomeState.value = DriverHomeUiState.Error
                }
                logD("result: $result")
//                viewModelScope.launch {
//                    homeEventChannel.send(HomeEvent.StartSearching(result))
//                }

            }
        }
    }

//    fun getPassengerName(id: String): String {
//        viewModelScope.launch {
//            passengerRepo.getPassengerName(id) {result ->
//                if (result is UiState.Success) {
//
//                }
//            }
//        }
//    }

//    fun onDisplayPlaces() {
//        val data = driverData!!
//        _currentHomeState.value = DriverHomeUiState.DisplayDriverPlaces(
//            driverLatLng = data.pickupLatLng,
//            driverDestinationLatLng = data.destinationLatLng,
//            driverAddress = data.pickup_title,
//            driverDestinationAddress = data.destination_title
//        )
//    }

    sealed class HomeEvent {
        data class UpdateResult(val state: UiState<Boolean>) : HomeEvent()
        data class StartSearching(val status: UiState<String>) : HomeEvent()
    }


}