package com.saleem.radeef.passenger.ui

import com.google.android.gms.maps.model.LatLng
import com.saleem.radeef.data.model.Ride
import com.saleem.radeef.data.model.Driver
import com.saleem.radeef.data.model.Vehicle


sealed interface PassengerHomeUiState {
    object SettingPlaces : PassengerHomeUiState

    data class DisplayPassengerPlaces(
        val pickupLatLng: LatLng,
        val destinationLatLng: LatLng,
        val distance: Double
    ) : PassengerHomeUiState

    data class WaitForDriverAcceptance(
        val ride: Ride,
        val distance: Double,
    ) : PassengerHomeUiState

    data class DisplayDriverOffer(
        val ride: Ride,
        val driver: Driver,
        val vehicle: Vehicle,
        val distance: Double,
    ) : PassengerHomeUiState


    data class PassengerPickUp(
        val ride: Ride,
        val driver: Driver,
        val vehicle: Vehicle,
        val distance: Double,

        ) : PassengerHomeUiState

    data class EnRoute(
        val ride: Ride,
        val distance: Double,
        val driver: Driver,
        val vehicle: Vehicle

    ) : PassengerHomeUiState

    data class Arrived(
        val ride: Ride,
        val driver: Driver,
        val distance: Double,
        val vehicle: Vehicle
    ) : PassengerHomeUiState

    object Error : PassengerHomeUiState
    object Loading : PassengerHomeUiState
}