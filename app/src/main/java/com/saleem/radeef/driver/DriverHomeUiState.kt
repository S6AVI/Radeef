package com.saleem.radeef.driver

import com.google.android.gms.maps.model.LatLng


sealed interface DriverHomeUiState {
    object SettingPlaces: DriverHomeUiState
    data class DisplayDriverPlaces(
        val driverLatLng: LatLng,
        val driverAddress: String,
        val driverDestinationLatLng: LatLng,
        val driverDestinationAddress: String,
        //val distance: Double
    ): DriverHomeUiState
    object SearchingForPassengers: DriverHomeUiState
    data class WaitPassengerResponse(
        val passengerLatLng: LatLng,
        val driverLatLng: LatLng,
        val passengerDestinationLatLng: LatLng,
        val passengerDestinationAddress: String,
        val passengerName: String,
    ): DriverHomeUiState

    data class PassengerPickUp(
        val passengerLatLng: LatLng,
        val driverLatLng: LatLng,
        val passengerDestinationLatLng: LatLng,
        val passengerDestinationAddress: String,
        val passengerName: String,
    ): DriverHomeUiState
    data class EnRoute(
        val driverLatLng: LatLng,
        val passengerDestinationLatLng: LatLng,
        val passengerDestinationAddress: String,
        val passengerName: String,
    ): DriverHomeUiState

    data class Arrived(
        val driverLatLng: LatLng,
        val passengerDestinationLatLng: LatLng,
        val passengerDestinationAddress: String,
        val driverDestinationLatLng: LatLng,
        val driverDestinationAddress: String,
        val passengerName: String,
    ): DriverHomeUiState

    data class ContinueRide(
        val driverLatLng: LatLng,
        val driverDestinationLatLng: LatLng,
        val driverDestinationAddress: String,
    ): DriverHomeUiState

    object Error: DriverHomeUiState
    object Loading: DriverHomeUiState
}