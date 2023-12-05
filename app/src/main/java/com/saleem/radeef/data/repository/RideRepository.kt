package com.saleem.radeef.data.repository

import com.saleem.radeef.data.model.Ride
import com.saleem.radeef.data.model.Driver
import com.saleem.radeef.util.RideWithDistance
import com.saleem.radeef.util.UiState

interface RideRepository {

    // get completed and cancelled rides
    fun getPassengerRides(result: (UiState<List<Ride>>) -> Unit)

    fun getDriverRides(result: (UiState<List<Ride>>) -> Unit)

    // create a new ride
    fun addRide(ride: Ride, result: (UiState<String>) -> Unit)

    // get all rides in SEARCHING state, and listen to changes
    fun getAllRidesRequests(result: (UiState<List<Ride>>) -> Unit)

    // hide ride; add it to Hidden_rides collection
    fun hideRide(rideId: String ,result: (UiState<String>) -> Unit)

    // update status of a ride; if driver accepts it
    fun updateRideStatus(rideWithDistance: RideWithDistance, driver: Driver, status: String, result: (UiState<String>) -> Unit)

    // get current ride of a driver
    fun getCurrentRide(result: (UiState<Ride?>) -> Unit)

    // hide ride when in WAITING state
    fun cancelWaitingRide(ride: Ride, result: (UiState<String>) -> Unit)

    // cancel ride of driver
    fun cancelRide(ride: Ride, result: (UiState<String>) -> Unit)

    // update current status of a ride
    fun updateCurrentRideState(ride: Ride, status: String, result: (UiState<String>) -> Unit)

    // get current ride of a passenger
    fun getPassengerCurrentRide(result: (UiState<Ride?>) -> Unit)

    // cancel ride of passenger
    fun cancelPassengerRide(ride: Ride, result: (UiState<String>) -> Unit)

    // change status of ride from WAITING to PICKUP; and update necessary fields
    fun confirmRide(ride: Ride, result: (UiState<String>) -> Unit)

}