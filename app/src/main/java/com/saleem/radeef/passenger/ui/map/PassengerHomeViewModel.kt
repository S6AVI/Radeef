package com.saleem.radeef.passenger.ui.map

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.maps.GeoApiContext
import com.saleem.radeef.data.RadeefLocation
import com.saleem.radeef.data.firestore.Passenger
import com.saleem.radeef.data.repository.CloudRepository
import com.saleem.radeef.data.repository.RideRepository
import com.saleem.radeef.driver.repo.DriverRepository
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
import com.saleem.radeef.data.firestore.Ride
import com.saleem.radeef.data.firestore.RideStatus
import com.saleem.radeef.data.firestore.driver.DriverWithVehicle
import com.saleem.radeef.passenger.PassengerHomeUiState
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


    var passengerData: Passenger? = null

    var currentLocation: RadeefLocation? = null

    private val _currentHomeState = MutableLiveData<PassengerHomeUiState>()
    val currentHomeState: LiveData<PassengerHomeUiState>
        get() = _currentHomeState


    init {
        logD("viewModel init is called")
        getPassenger()
    }

    private fun getPassenger() {
        _passenger.value = UiState.Loading
        repository.getPassenger { state ->

            if (state is UiState.Success) {
                logD("MapViewModel: in getPassenger: success: ${state.data}")
                passengerData = state.data

                val currentPassengerStatus = passengerData?.status


                if (currentPassengerStatus != null) {
                    setHomeUiState(currentPassengerStatus)
                }

            } else {
                logD("some problem in getPassenger")
            }
            _passenger.value = state
        }
    }


    fun updatePassengerLocations() {

        viewModelScope.launch {
            //updateResultChannel.send(UiState.Loading)
            homeEventChannel.send(DriverHomeViewModel.HomeEvent.UpdateResult(UiState.Loading))
            logD("pickup: ${pickup?.title}\ndestination: ${destination?.title}")
            if (pickup != null && destination != null) {
                repository.updatePassengerLocations(
                    pickup = pickup!!.latLng!!,
                    destination = destination!!.latLng!!
                ) { result ->
                    logD("viewModel: update destination - 78: $result")
                    viewModelScope.launch {
                        homeEventChannel.send(DriverHomeViewModel.HomeEvent.UpdateResult(result))
                    }
                }
            } else {
                logD("something is null")
            }
        }
    }

    fun setPassengerCurrentLocation(location: LatLng, address: String) {
        currentLocation = RadeefLocation(location, address)
    }


    private fun setHomeUiState(status: String) {
        logD("in home ui state setter - first line")

        val data = passengerData!!

        when (status) {
            PassengerStatus.INACTIVE.value -> {
                logD("location: ${data.destinationLatLng}")

                if (data.destinationLatLng.isDefault()) {
                    logD("location: set places")
                    _currentHomeState.value = PassengerHomeUiState.SettingPlaces
                } else {
                    logD("location: display places")
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
                            logD("ride status: ${ride.status}")
                            when (ride.status) {
                                RideStatus.SEARCHING_FOR_DRIVER.value -> {
                                    viewModelScope.launch {
                                        logD("ride status: SEARCHING")
                                        handleSearchingState(ride)
                                    }
                                }

                                RideStatus.WAITING_FOR_CONFIRMATION.value -> {
                                    viewModelScope.launch {
                                        logD("ride status: wait")
                                        handleWaitingForConfirmationStatus(ride)
                                    }
                                }

                                RideStatus.PASSENGER_PICK_UP.value -> {
                                    viewModelScope.launch {
                                        logD("ride status: pickup")
                                        handleInRideStates(ride)
                                    }
                                }

                                RideStatus.EN_ROUTE.value -> {
                                    viewModelScope.launch {
                                        logD("ride status: en_route")
                                        handleInRideStates(ride)
                                    }
                                }

                                RideStatus.ARRIVED.value -> {
                                    logD("arrived!")
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
                                logD("handle in_ride states: ${ride.status}")
                                handlePassengerPickup(ride, data)
                            }

                            RideStatus.EN_ROUTE.value -> {
                                logD("handle in_ride states: ${ride.status}")
                                handleEnRoute(ride, data)
                            }
                        }
                    }
                }
            }
        }
    }

    private suspend fun handlePassengerPickup(ride: Ride, data: DriverWithVehicle) {
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
        val distance =
            getDrivingDistanceInMeters(ride.passengerPickupLatLng, ride.passengerDestLatLng)
                ?: 0.0
        _currentHomeState.value = PassengerHomeUiState.WaitForDriverAcceptance(
            ride = ride,
            distance = distance.toKm()
        )
    }

    private suspend fun handleWaitingForConfirmationStatus(ride: Ride) {
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
        val distance =
            getDrivingDistanceInMeters(data.pickupLatLng, data.destinationLatLng) ?: 0.0
        _currentHomeState.value = PassengerHomeUiState.DisplayPassengerPlaces(
            pickupLatLng = data.pickupLatLng,
            destinationLatLng = data.destinationLatLng,
            distance = distance.toKm()
        )
    }


    fun onSearchButtonClicked(state: PassengerHomeUiState.DisplayPassengerPlaces) {
        _currentHomeState.value = PassengerHomeUiState.Loading
        val ride = createNewRide(state)
        viewModelScope.launch {
            ridesRepo.addRide(ride) { result ->
                if (result is UiState.Success) {

                }
            }
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

    private suspend fun getDrivingDistanceInMeters(
        origin: LatLng,
        destination: LatLng
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
                logD("time: ${leg.duration.humanReadable}")
                logD("distance: ${leg.distance.inMeters.toInt()}")
                leg.distance.inMeters.toDouble()
            } catch (e: Exception) {
                // Handle API exception
                e.printStackTrace()
                logD("error in calc distance: ${e.message}")
                null
            }
        }
    }

    fun onCancelButtonClicked(ride: Ride) {
        _currentHomeState.value = PassengerHomeUiState.Loading
        viewModelScope.launch {
            ridesRepo.cancelPassengerRide(ride) {

            }
        }
    }

    fun onConfirmButtonClicked(ride: Ride) {
        _currentHomeState.value = PassengerHomeUiState.Loading
        viewModelScope.launch {
            ridesRepo.confirmRide(ride) {

            }
        }
    }

    fun onDoneButtonClicked() {
        _currentHomeState.value = PassengerHomeUiState.Loading
        viewModelScope.launch {
            repository.updatePassengerState(PassengerStatus.INACTIVE.value) {

            }
        }
    }


}