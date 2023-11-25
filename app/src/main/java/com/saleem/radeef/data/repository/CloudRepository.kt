package com.saleem.radeef.data.repository

import com.google.android.gms.maps.model.LatLng
import com.saleem.radeef.data.firestore.Passenger
import com.saleem.radeef.util.UiState

interface CloudRepository {

    fun updatePassengerInfo(passenger: Passenger, result: (UiState<String>) -> Unit)

    fun getPassengerName(id: String, result: (UiState<String>) -> Unit)

    fun getPassenger(id: String, result: (UiState<Passenger?>) -> Unit)

    fun getPassenger(result: (UiState<Passenger>) -> Unit)
    fun updatePassengerLocations(pickup: LatLng, destination: LatLng, result: (UiState<Boolean>) -> Unit)
}