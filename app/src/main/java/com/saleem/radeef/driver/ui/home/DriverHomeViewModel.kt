package com.saleem.radeef.driver.ui.home

import androidx.fragment.app.Fragment
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polyline
import com.google.maps.DirectionsApi
import com.google.maps.GeoApiContext
import com.google.maps.model.TravelMode
import com.google.maps.model.Unit
import com.saleem.radeef.R
import com.saleem.radeef.data.RadeefLocation
import com.saleem.radeef.data.firestore.Ride
import com.saleem.radeef.data.firestore.RideStatus
import com.saleem.radeef.data.firestore.driver.Driver
import com.saleem.radeef.data.firestore.driver.UserStatus
import com.saleem.radeef.data.repository.CloudRepository
import com.saleem.radeef.data.repository.RideRepository
import com.saleem.radeef.driver.DriverHomeUiState
import com.saleem.radeef.driver.repo.DriverRepository
import com.saleem.radeef.util.MAX_DISTANCE_METERS_THRESHOLD
import com.saleem.radeef.util.Permissions
import com.saleem.radeef.util.UiState
import com.saleem.radeef.util.logD
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception

class DriverHomeViewModel @ViewModelInject constructor(
    val repository: DriverRepository,
    private val ridesRepo: RideRepository,
    val geoContext: GeoApiContext,
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


    private val _rideRequests = MutableLiveData<UiState<List<RideWithDistance>>>()
    val rideRequests: LiveData<UiState<List<RideWithDistance>>>
        get() = _rideRequests

    // Method to update the current state
    private fun updateDriverState(newState: DriverHomeUiState) {
        _currentHomeState.value = newState
    }

    private val drawnPolylines: MutableList<Polyline> = mutableListOf()

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

    fun setHomeUiState(status: String) {
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
                ridesRepo.getCurrentRide { result ->
                    if (result is UiState.Success) {
                        logD("inside viewmodel - searching state of driver - ride: ${result.data}")
                        if (result.data != null) {
                            val ride = result.data

                            when (ride.status) {
                                RideStatus.WAITING_FOR_CONFIRMATION.value -> {
                                    _currentHomeState.value =
                                        DriverHomeUiState.WaitPassengerResponse(
                                            ride = ride,
                                            passengerName = ride.passengerName,
                                            passengerPickupLatLng = ride.passengerPickupLatLng,
                                            passengerDestinationLatLng = ride.passengerDestLatLng,
                                            driverLatLng = ride.driverLocationLatLng,
                                            distance = ride.distance,
                                            cost = ride.chargeAmount,
                                            passengerId = ride.passengerID
                                        )
                                }

                                else -> {
                                    _currentHomeState.value =
                                        DriverHomeUiState.SearchingForPassengers
                                }
                            }
                        } else {
                            _currentHomeState.value = DriverHomeUiState.SearchingForPassengers
                        }
                    }
                }
            }

            UserStatus.IN_RIDE.value -> {
                ridesRepo.getCurrentRide { result ->
                    if (result is UiState.Success) {
                        logD("inside viewModel - in_ride state of driver - ride: ${result.data}")
                        if (result.data != null) {
                            val ride = result.data

                            when (ride.status) {
                                RideStatus.PASSENGER_PICK_UP.value -> {
                                    logD("ride status: ride confirmed!")
                                    viewModelScope.launch {
                                        handlePassengerPickupState(ride, ride.status)
                                    }
                                }

                                RideStatus.EN_ROUTE.value -> {
                                    viewModelScope.launch {
                                        handleEnRouteState(ride, ride.status)
                                    }

                                }

                                RideStatus.CANCELED.value -> {
                                    _currentHomeState.value = DriverHomeUiState.SearchingForPassengers
                                }

                                else -> {
                                    // Handle other ride statuses if necessary
                                }
                            }
                        } else {
                            _currentHomeState.value = DriverHomeUiState.SearchingForPassengers
                        }
                    }
                }
            }
        }
        //_currentHomeState.value = DriverHomeUiState.SettingPlaces
    }

    private suspend fun handlePassengerPickupState(ride: Ride, status: String) {
        val distance = getDrivingDistance(driverData!!.pickupLatLng, ride.passengerPickupLatLng) ?: 0.0
        _currentHomeState.value = DriverHomeUiState.PassengerPickUp(ride, distance)
    }

    private suspend fun handleEnRouteState(ride: Ride, status: String) {
        val distance = getDrivingDistance(ride.passengerPickupLatLng, ride.passengerDestLatLng) ?: 0.0
        _currentHomeState.value = DriverHomeUiState.EnRoute(ride, distance)
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
//            logD("pickup: ${pickup?.title}\n destination: ${destination?.title}")
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


    fun fetchRideRequests() {
        _rideRequests.value = UiState.Loading
        viewModelScope.launch {
            ridesRepo.getAllRidesRequests { result ->
                if (result is UiState.Success) {
                    logD("all rides length: ${result.data.size}")
                    viewModelScope.launch {
                        val filteredRides = withContext(Dispatchers.IO) {
                            filterRidesByProximity(result.data)
                        }
                        _rideRequests.value = UiState.Success(filteredRides)
                    }
                } else if (result is UiState.Failure) {
                    _rideRequests.value = result
                }
            }
        }
    }


    private suspend fun filterRidesByProximity(rides: List<Ride>): List<RideWithDistance> {
        val filteredRides = mutableListOf<RideWithDistance>()

        for (ride in rides) {
            logD("passenger pickup LatLng: ${ride.passengerPickupLatLng}")

            val pickupDistance = withContext(Dispatchers.IO) {

                getDrivingDistance(driverData!!.pickupLatLng, ride.passengerPickupLatLng)
            }
            val destinationDistance = withContext(Dispatchers.IO) {
                getDrivingDistance(driverData!!.destinationLatLng, ride.passengerDestLatLng)
            }


            if (pickupDistance != null && destinationDistance != null &&
                pickupDistance <= MAX_DISTANCE_METERS_THRESHOLD && destinationDistance <= MAX_DISTANCE_METERS_THRESHOLD
            ) {
                val totalDistance = pickupDistance + destinationDistance
                filteredRides.add(RideWithDistance(ride, totalDistance))
            }
        }
        return filteredRides
    }

    private suspend fun getDrivingDistance(
        origin: LatLng,
        destination: LatLng,
    ): Double? {
        logD("origin: $origin")
        logD("dest: $destination")
        return withContext(Dispatchers.IO) {
            val request = DirectionsApi.newRequest(geoContext)
                .mode(TravelMode.DRIVING)
                .origin(
                    com.google.maps.model.LatLng(origin.latitude, origin.longitude)
                )
                .destination(
                    com.google.maps.model.LatLng(destination.latitude, destination.longitude)
                )
                .units(Unit.METRIC)
            try {
                val result = request.await()
                val route = result.routes[0]
                val leg = route.legs[0]
                logD("distance: ${leg.distance.inMeters.toInt()}")
                leg.distance.inMeters.toInt() / 1000.0
            } catch (e: Exception) {
                // Handle API exception
                e.printStackTrace()
                logD("error in calc distance: ${e.message}")
                null
            }
        }
    }

    fun onAdapterItemClicked(rideWithDistance: RideWithDistance) {
        logD("item click: Not yet implemented")
    }

    fun onAdapterRideAccept(rideWithDistance: RideWithDistance, cost: Double) {
        logD("item accept: Not yet implemented")
        _currentHomeState.value = DriverHomeUiState.Loading

        viewModelScope.launch {
            ridesRepo.updateRideState(
                rideWithDistance = rideWithDistance.copy(
                    ride = rideWithDistance.ride.copy(
                        chargeAmount = cost
                    )
                ),
                status = RideStatus.WAITING_FOR_CONFIRMATION.value,
                driver = Driver(
                    driverID = driverData!!.driverID,
                    name = driverData!!.name,
                    pickup = driverData!!.pickup
                ),
            ) { result ->
                if (result is UiState.Success) {
                    val ride = rideWithDistance.ride
                    logD("cost: ${ride.chargeAmount}")
                    _currentHomeState.value = DriverHomeUiState.WaitPassengerResponse(
                        passengerDestinationLatLng = ride.passengerDestLatLng,
                        passengerPickupLatLng = ride.passengerPickupLatLng,
                        passengerName = ride.passengerName,
                        driverLatLng = ride.driverLocationLatLng,
                        distance = rideWithDistance.distance,
                        cost = cost,
                        ride = ride,
                        passengerId = ride.passengerID
                    )
                }
            }
        }
    }

    fun onAdapterRideHide(rideId: String) {
        //logD("item hide: Not yet implemented")
        viewModelScope.launch {
            ridesRepo.hideRide(rideId) { result ->
                if (result is UiState.Success) {
                    logD("success in hiding: ${result.data}")
                } else {
                    logD("error in hiding")
                }

            }
        }
    }


    fun addPolyline(polyline: Polyline) {
        drawnPolylines.add(polyline)
    }

    fun removePolylines() {
        for (polyline in drawnPolylines) {
            polyline.remove()
        }
        drawnPolylines.clear()
    }

    fun onCancelButtonClickedWaiting(ride: Ride) {
        _currentHomeState.value = DriverHomeUiState.Loading
        viewModelScope.launch {
            ridesRepo.cancelWaitingRide(ride) { result ->
                if (result is UiState.Success) {
                    _currentHomeState.value = DriverHomeUiState.SearchingForPassengers
                } else {
                    logD("cancel in waiting state: error: $result")
                }
            }
        }
    }

    fun onCallPassenger(passengerId: String) {
        viewModelScope.launch {
            passengerRepo.getPassenger(passengerId) { result ->
                if (result is UiState.Success) {
                    viewModelScope.launch {
                        homeEventChannel.send(HomeEvent.CallPassenger(phoneNumber = result.data!!.phoneNumber))
                    }
                } else {
                    logD("some error occurred: $result")
                }
            }
        }
    }

    fun onCancelButton(ride: Ride) {
        _currentHomeState.value = DriverHomeUiState.Loading
        logD("viewModel - on cancel ride button clicked")
        viewModelScope.launch {
            ridesRepo.cancelRide(ride) {result ->
                if (result is UiState.Success) {
                    _currentHomeState.value = DriverHomeUiState.SearchingForPassengers
                }
            }
        }
    }

    fun onDriverArrivedToPassenger(ride: Ride, state: DriverHomeUiState.PassengerPickUp) {
        _currentHomeState.value = DriverHomeUiState.Loading
        viewModelScope.launch {
            ridesRepo.updateCurrentRideState(ride) {result ->
                if (result is UiState.Success) {
                    //_currentHomeState.value = DriverHomeUiState.SearchingForPassengers
                }
            }
        }
    }


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
        data class CallPassenger(val phoneNumber: String) : HomeEvent()
    }


}

