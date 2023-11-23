package com.saleem.radeef.data.repository

import com.saleem.radeef.data.firestore.Ride
import com.saleem.radeef.data.firestore.driver.Driver
import com.saleem.radeef.driver.ui.home.RideWithDistance
import com.saleem.radeef.util.UiState

interface RideRepository {

    fun getPassengerRides(result: (UiState<List<Ride>>) -> Unit)

    fun getDriverRides(result: (UiState<List<Ride>>) -> Unit)
    fun addRide(ride: Ride, result: (UiState<String>) -> Unit)

    //fun getDriverRides(result: (UiState<List<Ride>>) -> Unit)

    fun getAllRidesRequests(result: (UiState<List<Ride>>) -> Unit)

    fun hideRide(rideId: String ,result: (UiState<String>) -> Unit)

    fun updateRideState(rideWithDistance: RideWithDistance, driver: Driver, status: String, result: (UiState<String>) -> Unit)

    fun getCurrentRide(result: (UiState<Ride?>) -> Unit)
    fun cancelWaitingRide(ride: Ride, result: (UiState<String>) -> Unit)
    fun cancelRide(ride: Ride, result: (UiState<String>) -> Unit)

    fun updateCurrentRideState(ride: Ride, status: String, result: (UiState<String>) -> Unit)
}