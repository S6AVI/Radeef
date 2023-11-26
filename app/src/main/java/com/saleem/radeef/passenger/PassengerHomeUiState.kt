package com.saleem.radeef.passenger

import com.google.android.gms.maps.model.LatLng
import com.saleem.radeef.data.firestore.Ride
import com.saleem.radeef.data.firestore.driver.Driver
import com.saleem.radeef.data.firestore.driver.Vehicle


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
        val driver: Driver

    ) : PassengerHomeUiState

    data class Arrived(
        val ride: Ride,
        val driver: Driver
    ) : PassengerHomeUiState

    object Error : PassengerHomeUiState
    object Loading : PassengerHomeUiState
}