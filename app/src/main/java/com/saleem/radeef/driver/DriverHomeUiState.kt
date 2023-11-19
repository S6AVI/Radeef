package com.saleem.radeef.driver

import com.google.android.gms.maps.model.LatLng
import com.saleem.radeef.data.firestore.Ride


sealed interface DriverHomeUiState {
    object SettingPlaces : DriverHomeUiState
    data class DisplayDriverPlaces(
        val driverLatLng: LatLng,
        val driverAddress: String,
        val driverDestinationLatLng: LatLng,
        val driverDestinationAddress: String,
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
        val driverLatLng: LatLng,
        val passengerDestinationLatLng: LatLng,
        val passengerDestinationAddress: String,
        val driverDestinationLatLng: LatLng,
        val driverDestinationAddress: String,
        val passengerName: String,
    ) : DriverHomeUiState

    data class ContinueRide(
        val driverLatLng: LatLng,
        val driverDestinationLatLng: LatLng,
        val driverDestinationAddress: String,
    ) : DriverHomeUiState

    object Error : DriverHomeUiState
    object Loading : DriverHomeUiState
}