package com.saleem.radeef.driver

import com.google.android.gms.maps.model.LatLng
import com.saleem.radeef.data.firestore.Ride


sealed interface DriverHomeUiState {
    object SettingPlaces : DriverHomeUiState
    data class DisplayDriverPlaces(
        val driverLatLng: LatLng,
        val driverDestinationLatLng: LatLng,
        val distance: Double
        //val distance: Double
    ) : DriverHomeUiState

    object SearchingForPassengers : DriverHomeUiState
    data class WaitPassengerResponse(
        val ride: Ride,
        val passengerId: String,
        val passengerPickupLatLng: LatLng,
        //val passengerPickupAddress: String,
        val passengerDestinationLatLng: LatLng,
        //val passengerDestinationAddress: String,
        val passengerName: String,
        val driverLatLng: LatLng,
        val distance: Double,
        val cost: Double
    ) : DriverHomeUiState

    data class PassengerPickUp(
        val ride: Ride,
        val distance: Double
//        val passengerLatLng: LatLng,
//        val driverLatLng: LatLng,
//        val passengerDestinationLatLng: LatLng,
//        val passengerDestinationAddress: String,
//        val passengerName: String,
    ) : DriverHomeUiState

    data class EnRoute(
        val ride: Ride,
        val distance: Double
//        val driverLatLng: LatLng,
//        val passengerDestinationLatLng: LatLng,
//        val passengerDestinationAddress: String,
//        val passengerName: String,
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