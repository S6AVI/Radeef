package com.saleem.radeef.passenger.ui.home

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.maps.GeoApiContext
import com.saleem.radeef.data.model.RadeefLocation
import com.saleem.radeef.data.model.Passenger
import com.saleem.radeef.data.repository.CloudRepository
import com.saleem.radeef.data.repository.RideRepository
import com.saleem.radeef.data.repository.DriverRepository
import com.saleem.radeef.driver.ui.home.DriverHomeViewModel
import com.saleem.radeef.util.UiState
import com.saleem.radeef.util.logD
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import com.google.android.gms.maps.model.LatLng
import com.google.maps.DirectionsApi
import com.google.maps.model.TravelMode
import com.google.maps.model.Unit
import com.saleem.radeef.data.model.Ride
import com.saleem.radeef.util.RideStatus
import com.saleem.radeef.data.model.DriverWithVehicle
import com.saleem.radeef.passenger.ui.PassengerHomeUiState
import com.saleem.radeef.util.PassengerStatus
import com.saleem.radeef.util.isDefault
import com.saleem.radeef.util.toGeoPoint
import com.saleem.radeef.util.toKm
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.Exception
import java.util.Date

class PassengerHomeViewModel @ViewModelInject constructor(
    val repository: CloudRepository,
    private val ridesRepo: RideRepository,
    private val geoContext: GeoApiContext,
    private val driverRepo: DriverRepository
) : ViewModel() {

    var pickup: RadeefLocation? = null
    var destination: RadeefLocation? = null


    private val _passenger = MutableLiveData<UiState<Passenger>>()
    val passenger: LiveData<UiState<Passenger>>
        get() = _passenger

    private val homeEventChannel = Channel<DriverHomeViewModel.HomeEvent>()
    val homeEvent = homeEventChannel.receiveAsFlow()


    private var passengerData: Passenger? = null

    var currentLocation: RadeefLocation? = null

    private val _currentHomeState = MutableLiveData<PassengerHomeUiState>()
    val currentHomeState: LiveData<PassengerHomeUiState>
        get() = _currentHomeState


    init {
        getPassenger()
    }

    private fun getPassenger() {
        _passenger.value = UiState.Loading
        repository.getPassenger { state ->

            if (state is UiState.Success) {
                passengerData = state.data

                val currentPassengerStatus = passengerData?.status

                if (currentPassengerStatus != null) {
                    setHomeUiState(currentPassengerStatus)
                }
            }
            _passenger.value = state
        }
    }


    fun updatePassengerLocations() {

        viewModelScope.launch {
            homeEventChannel.send(DriverHomeViewModel.HomeEvent.UpdateResult(UiState.Loading))

            if (pickup != null && destination != null) {
                repository.updatePassengerLocations(
                    pickup = pickup!!.latLng!!,
                    destination = destination!!.latLng!!
                ) { result ->
                    viewModelScope.launch {
                        homeEventChannel.send(DriverHomeViewModel.HomeEvent.UpdateResult(result))
                    }
                }
            }
        }
    }

    fun setPassengerCurrentLocation(location: LatLng, address: String) {
        currentLocation = RadeefLocation(location, address)
    }


    private fun setHomeUiState(status: String) {

        val data = passengerData!!

        when (status) {
            PassengerStatus.INACTIVE.value -> {

                if (data.destinationLatLng.isDefault()) {

                    _currentHomeState.value = PassengerHomeUiState.SettingPlaces
                } else {
                    viewModelScope.launch {
                        handleDisplayPlacesState(data)
                    }

                }
            }

            else -> {
                ridesRepo.getPassengerCurrentRide { result ->
                    if (result is UiState.Success) {
                        if (result.data != null) {
                            val ride = result.data
                            when (ride.status) {
                                RideStatus.SEARCHING_FOR_DRIVER.value -> {
                                    viewModelScope.launch {
                                        handleSearchingState(ride)
                                    }
                                }

                                RideStatus.WAITING_FOR_CONFIRMATION.value -> {
                                    viewModelScope.launch {
                                        handleWaitingForConfirmationStatus(ride)
                                    }
                                }

                                RideStatus.PASSENGER_PICK_UP.value -> {
                                    viewModelScope.launch {
                                        handleInRideStates(ride)
                                    }
                                }

                                RideStatus.EN_ROUTE.value -> {
                                    viewModelScope.launch {
                                        handleInRideStates(ride)
                                    }
                                }

                                RideStatus.ARRIVED.value -> {
                                    handleArrivedState(ride)
                                }

                            }
                        }
                    }
                }
            }
        }
    }

    private fun handleArrivedState(ride: Ride) {
        _currentHomeState.value = PassengerHomeUiState.Loading
        viewModelScope.launch {
            driverRepo.getDriverWhenArrived(ride.driverId) { result ->
                if (result is UiState.Success) {
                    val data = result.data!!
                    viewModelScope.launch {
                        val distance =
                            getDrivingDistanceInMeters(
                                ride.passengerPickupLatLng,
                                ride.passengerDestLatLng
                            ) ?: 0.0
                        _currentHomeState.value = PassengerHomeUiState.Arrived(
                            ride = ride,
                            driver = data.driver!!,
                            vehicle = data.vehicle!!,
                            distance = distance.toKm()
                        )
                    }
                }
            }
        }
    }

    private fun handleInRideStates(ride: Ride) {
        _currentHomeState.value = PassengerHomeUiState.Loading
        viewModelScope.launch {
            driverRepo.getDriver(ride.driverId) { result ->
                if (result is UiState.Success) {
                    val data = result.data!!
                    viewModelScope.launch {
                        when (ride.status) {
                            RideStatus.PASSENGER_PICK_UP.value -> {
                                handlePassengerPickup(ride, data)
                            }

                            RideStatus.EN_ROUTE.value -> {
                                handleEnRoute(ride, data)
                            }
                        }
                    }
                }
            }
        }
    }

    private suspend fun handlePassengerPickup(ride: Ride, data: DriverWithVehicle) {
        _currentHomeState.value = PassengerHomeUiState.Loading
        viewModelScope.launch {
            val distance =
                getDrivingDistanceInMeters(
                    data.driver!!.pickupLatLng,
                    ride.passengerPickupLatLng
                ) ?: 0.0
            _currentHomeState.value = PassengerHomeUiState.PassengerPickUp(
                ride = ride,
                driver = data.driver,
                vehicle = data.vehicle!!,
                distance = distance.toKm()
            )
        }
    }

    private suspend fun handleEnRoute(ride: Ride, data: DriverWithVehicle) {
        _currentHomeState.value = PassengerHomeUiState.Loading
        viewModelScope.launch {
            val distance =
                getDrivingDistanceInMeters(
                    data.driver!!.pickupLatLng,
                    ride.passengerDestLatLng
                ) ?: 0.0
            _currentHomeState.value = PassengerHomeUiState.EnRoute(
                ride = ride,
                driver = data.driver,
                vehicle = data.vehicle!!,
                distance = distance.toKm()
            )
        }
    }


    private suspend fun handleSearchingState(ride: Ride) {
        _currentHomeState.value = PassengerHomeUiState.Loading
        val distance =
            getDrivingDistanceInMeters(ride.passengerPickupLatLng, ride.passengerDestLatLng)
                ?: 0.0
        _currentHomeState.value = PassengerHomeUiState.WaitForDriverAcceptance(
            ride = ride,
            distance = distance.toKm()
        )
    }

    private suspend fun handleWaitingForConfirmationStatus(ride: Ride) {
        _currentHomeState.value = PassengerHomeUiState.Loading
        val distance =
            getDrivingDistanceInMeters(ride.passengerPickupLatLng, ride.passengerDestLatLng)
                ?: 0.0
        viewModelScope.launch {
            driverRepo.getDriver(ride.driverId) { result ->
                if (result is UiState.Success) {
                    val data = result.data!!
                    _currentHomeState.value = PassengerHomeUiState.DisplayDriverOffer(
                        ride = ride,
                        driver = data.driver!!,
                        vehicle = data.vehicle!!,
                        distance = distance.toKm()
                    )
                }
            }
        }
    }


    private suspend fun handleDisplayPlacesState(data: Passenger) {
        _currentHomeState.value = PassengerHomeUiState.Loading
        val distance =
            getDrivingDistanceInMeters(data.pickupLatLng, data.destinationLatLng) ?: 0.0
        _currentHomeState.value = PassengerHomeUiState.DisplayPassengerPlaces(
            pickupLatLng = data.pickupLatLng,
            destinationLatLng = data.destinationLatLng,
            distance = distance.toKm()
        )
    }

    private suspend fun getDrivingDistanceInMeters(origin: LatLng, destination: LatLng
    ): Double? {
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
                logD("error in getDrivingDistanceInMeters: ${e.message}")
                null
            }
        }
    }

    fun onSearchButtonClicked(state: PassengerHomeUiState.DisplayPassengerPlaces) {
        _currentHomeState.value = PassengerHomeUiState.Loading
        val ride = createNewRide(state)
        viewModelScope.launch {
            ridesRepo.addRide(ride) {}
        }
    }

    fun onCancelButtonClicked(ride: Ride) {
        _currentHomeState.value = PassengerHomeUiState.Loading
        viewModelScope.launch {
            ridesRepo.cancelPassengerRide(ride) {}
        }
    }

    fun onConfirmButtonClicked(ride: Ride) {
        _currentHomeState.value = PassengerHomeUiState.Loading
        viewModelScope.launch {
            ridesRepo.confirmRide(ride) {}
        }
    }

    fun onDoneButtonClicked() {
        _currentHomeState.value = PassengerHomeUiState.Loading
        viewModelScope.launch {
            repository.updatePassengerStatus(PassengerStatus.INACTIVE.value) {}
        }
    }

    private fun createNewRide(state: PassengerHomeUiState.DisplayPassengerPlaces): Ride {
        return Ride(
            passengerPickupLocation = state.pickupLatLng.toGeoPoint(),
            passengerDestination = state.destinationLatLng.toGeoPoint(),
            startTime = Date(),
            passengerID = passengerData!!.passengerID,
            passengerName = passengerData!!.name,
            status = RideStatus.SEARCHING_FOR_DRIVER.value,
        )
    }

}