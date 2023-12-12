package com.saleem.radeef.driver.ui.home

import androidx.fragment.app.Fragment
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.maps.DirectionsApi
import com.google.maps.GeoApiContext
import com.google.maps.model.TravelMode
import com.google.maps.model.Unit
import com.saleem.radeef.R
import com.saleem.radeef.data.model.RadeefLocation
import com.saleem.radeef.data.model.Ride
import com.saleem.radeef.util.RideStatus
import com.saleem.radeef.data.model.Driver
import com.saleem.radeef.util.DriverStatus
import com.saleem.radeef.data.repository.CloudRepository
import com.saleem.radeef.data.repository.RideRepository
import com.saleem.radeef.driver.ui.DriverHomeUiState
import com.saleem.radeef.data.repository.DriverRepository
import com.saleem.radeef.util.HomeEvent
import com.saleem.radeef.util.MAX_DISTANCE_METERS_THRESHOLD
import com.saleem.radeef.util.Permissions
import com.saleem.radeef.util.RideWithDistance
import com.saleem.radeef.util.UiState
import com.saleem.radeef.util.isDefault
import com.saleem.radeef.util.logD
import com.saleem.radeef.util.toKm
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception
import java.util.Date
import kotlin.math.abs

class DriverHomeViewModel @ViewModelInject constructor(
    val repository: DriverRepository,
    private val ridesRepo: RideRepository,
    private val geoContext: GeoApiContext,
    private val passengerRepo: CloudRepository
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


    private val homeEventChannel = Channel<HomeEvent>()
    val homeEvent = homeEventChannel.receiveAsFlow()


    private fun setHomeUiState(status: String) {

        val data = driverData!!

        var searchingStateSet = false

        when (status) {
            DriverStatus.INACTIVE.value -> {

                if (data.destinationLatLng.isDefault()) {

                    _currentHomeState.value = DriverHomeUiState.SettingPlaces
                } else {
                    viewModelScope.launch {
                        _currentHomeState.value = DriverHomeUiState.DisplayDriverPlaces(
                            driverLatLng = data.pickupLatLng,
                            driverDestinationLatLng = data.destinationLatLng,
                            distance = getDrivingDistanceInMeters(data.pickupLatLng, data.destinationLatLng)?.toKm()
                                ?: 0.0
                        )
                    }
                }
            }

            DriverStatus.SEARCHING.value -> {
                ridesRepo.getCurrentRide { result ->
                    if (result is UiState.Success) {

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
                                    searchingStateSet = true
                                }
                            }
                        } else {
                            _currentHomeState.value = DriverHomeUiState.SearchingForPassengers
                            searchingStateSet = true
                        }
                    }
                }
                if (searchingStateSet) {
                    return
                }
            }

            DriverStatus.IN_RIDE.value -> {
                ridesRepo.getCurrentRide { result ->
                    if (result is UiState.Success) {
                        if (result.data != null) {
                            val ride = result.data

                            when (ride.status) {
                                RideStatus.PASSENGER_PICK_UP.value -> {
                                    viewModelScope.launch {
                                        handlePassengerPickupState(ride)
                                    }
                                }

                                RideStatus.EN_ROUTE.value -> {
                                    viewModelScope.launch {
                                        handleEnRouteState(ride)
                                    }

                                }

                                RideStatus.ARRIVED.value -> {
                                    viewModelScope.launch {
                                        handleArrivedState(ride)
                                    }
                                }

                                RideStatus.CANCELED.value -> {
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

            DriverStatus.CONTINUE.value -> {
                viewModelScope.launch {
                    handleContinueStatus()
                }
            }
        }
    }

    private suspend fun handleArrivedState(ride: Ride) {
        val distance =
            getDrivingDistanceInMeters(driverData!!.pickupLatLng, driverData!!.destinationLatLng) ?: 0.0
        _currentHomeState.value = DriverHomeUiState.Arrived(ride)
    }

    private suspend fun handleContinueStatus() {
        val distance =
            getDrivingDistanceInMeters(driverData!!.pickupLatLng, driverData!!.destinationLatLng) ?: 0.0
        _currentHomeState.value = DriverHomeUiState.ContinueRide(distance.toKm())
    }

    private suspend fun handlePassengerPickupState(ride: Ride) {
        val distance =
            getDrivingDistanceInMeters(driverData!!.pickupLatLng, ride.passengerPickupLatLng) ?: 0.0
        _currentHomeState.value = DriverHomeUiState.PassengerPickUp(ride, distance.toKm())
    }

    private suspend fun handleEnRouteState(ride: Ride) {
        val distance =
            getDrivingDistanceInMeters(ride.passengerPickupLatLng, ride.passengerDestLatLng) ?: 0.0
        _currentHomeState.value = DriverHomeUiState.EnRoute(ride, distance.toKm())
    }

    private fun getDriver() {
        _driver.value = UiState.Loading
        repository.getDriver { state ->

            if (state is UiState.Success) {
                driverData = state.data

                val currentDriverStatus = driverData?.status

                if (currentDriverStatus != null) {
                    setHomeUiState(currentDriverStatus)
                }
            } else {
                logD("some problem in getDriver")
            }
            _driver.value = state
        }
    }

    init {
        getDriver()
    }



    fun updateDriverLocations() {
        viewModelScope.launch {

            homeEventChannel.send(HomeEvent.UpdateResult(UiState.Loading))
            if (destination != null) {
                repository.updateDriverDestination(destination = destination!!.latLng!!) { result ->

                    viewModelScope.launch {
                        homeEventChannel.send(HomeEvent.UpdateResult(result))
                        setHomeUiState(driverData!!.status)
                    }
                }
            }
        }
    }

    fun onSearchButtonClicked() {
        _currentHomeState.value = DriverHomeUiState.Loading
        viewModelScope.launch {

            repository.updateDriver(
                driverData!!.copy(status = DriverStatus.SEARCHING.value)
            ) { result ->
                if (result is UiState.Success) {
                    driverData = driverData!!.copy(status = DriverStatus.SEARCHING.value)
                    _currentHomeState.value = DriverHomeUiState.SearchingForPassengers
                } else {
                    logD("error in onSearchButtonClicked")
                }
            }
        }
    }


    fun fetchRideRequests() {
        _rideRequests.value = UiState.Loading
        viewModelScope.launch {
            ridesRepo.getAllRidesRequests { result ->
                if (result is UiState.Success) {
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

            val pickupDistance = withContext(Dispatchers.IO) {
                getDrivingDistanceInMeters(driverData!!.pickupLatLng, ride.passengerPickupLatLng)
            }
            val destinationDistance = withContext(Dispatchers.IO) {
                getDrivingDistanceInMeters(ride.passengerDestLatLng, driverData!!.destinationLatLng)
            }


            val originalRideDistance = withContext(Dispatchers.IO) {
                getDrivingDistanceInMeters(driverData!!.pickupLatLng, driverData!!.destinationLatLng)
            }

            val rideDistance = withContext(Dispatchers.IO) {
                getDrivingDistanceInMeters(ride.passengerPickupLatLng, ride.passengerDestLatLng)
            }


            if (pickupDistance != null && destinationDistance != null && originalRideDistance != null && rideDistance != null) {
                val netDistance = (pickupDistance + destinationDistance) + abs(originalRideDistance - rideDistance)
                if (netDistance <= MAX_DISTANCE_METERS_THRESHOLD) {
                    filteredRides.add(RideWithDistance(ride, netDistance.toKm()))
                }
            }
        }
        return filteredRides
    }

    private suspend fun getDrivingDistanceInMeters(origin: LatLng, destination: LatLng): Double? {

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
                leg.distance.inMeters.toDouble()
            } catch (e: Exception) {
                logD("error in calc distance: ${e.message}")
                null
            }
        }
    }

    fun onAdapterRideAccept(rideWithDistance: RideWithDistance, cost: Double) {
        _currentHomeState.value = DriverHomeUiState.Loading

        viewModelScope.launch {
            ridesRepo.updateRideStatus(
                rideWithDistance = rideWithDistance.copy(
                    ride = rideWithDistance.ride.copy(
                        chargeAmount = cost,
                        startTime = Date()
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
        viewModelScope.launch {
            ridesRepo.hideRide(rideId) {}
        }
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
        viewModelScope.launch {
            ridesRepo.cancelRide(ride) { result ->
                if (result is UiState.Success) {
                    _currentHomeState.value = DriverHomeUiState.SearchingForPassengers
                }
            }
        }
    }

    fun onDriverArrivedToPassenger(ride: Ride) {
        _currentHomeState.value = DriverHomeUiState.Loading
        viewModelScope.launch {
            ridesRepo.updateCurrentRideState(ride, RideStatus.EN_ROUTE.value) {}
        }
    }

    fun onArrivedToPassengerDestination(ride: Ride) {
        _currentHomeState.value = DriverHomeUiState.Loading
        viewModelScope.launch {
            ridesRepo.updateCurrentRideState(ride, RideStatus.ARRIVED.value) {}
        }
    }

    fun onStopButtonClicked() {
        _currentHomeState.value = DriverHomeUiState.Loading
        viewModelScope.launch {
            repository.updateDriver(
                driverData!!.copy(status = DriverStatus.INACTIVE.value)
            ) {}
        }
    }

    fun onContinueButtonClicked() {
        _currentHomeState.value = DriverHomeUiState.Loading
        viewModelScope.launch {
            repository.updateDriver(
                driverData!!.copy(status = DriverStatus.CONTINUE.value)
            ) {}
        }
    }



    fun onDoneButtonClicked() {
        _currentHomeState.value = DriverHomeUiState.Loading
        viewModelScope.launch {
            repository.updateDriver(
                driverData!!.copy(status = DriverStatus.INACTIVE.value)
            ) {}
        }
    }


    fun updateLocation(location: LatLng) {
        viewModelScope.launch {
            repository.updateDriverCurrentLocation(location) { }
        }
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
        val mapFragment =
            fragment.childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(fragment)
    }


}

