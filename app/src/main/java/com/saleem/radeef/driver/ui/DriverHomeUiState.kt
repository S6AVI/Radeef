package com.saleem.radeef.driver.ui

import com.google.android.gms.maps.model.LatLng
import com.saleem.radeef.data.model.Ride


sealed interface DriverHomeUiState {
    object SettingPlaces : DriverHomeUiState
    data class DisplayDriverPlaces(
        val driverLatLng: LatLng,
        val driverDestinationLatLng: LatLng,
        val distance: Double
    ) : DriverHomeUiState

    object SearchingForPassengers : DriverHomeUiState
    data class WaitPassengerResponse(
        val ride: Ride,
        val passengerId: String,
        val passengerPickupLatLng: LatLng,
        val passengerDestinationLatLng: LatLng,
        val passengerName: String,
        val driverLatLng: LatLng,
        val distance: Double,
        val cost: Double
    ) : DriverHomeUiState

    data class PassengerPickUp(
        val ride: Ride,
        val distance: Double
    ) : DriverHomeUiState

    data class EnRoute(
        val ride: Ride,
        val distance: Double
    ) : DriverHomeUiState

    data class Arrived(
        val ride: Ride
    ) : DriverHomeUiState

    data class ContinueRide (
        val distance: Double
    ) : DriverHomeUiState

    object Error : DriverHomeUiState
    object Loading : DriverHomeUiState
}