package com.saleem.radeef.data.repository

import com.google.android.gms.maps.model.LatLng
import com.saleem.radeef.data.model.Passenger
import com.saleem.radeef.util.UiState

interface CloudRepository {

    // update passenger info; create a document if it's a new passenger
    fun updatePassengerInfo(passenger: Passenger, result: (UiState<String>) -> Unit)

    // get passenger name
    fun getPassengerName(id: String, result: (UiState<String>) -> Unit)

    // get passenger; for driver
    fun getPassenger(id: String, result: (UiState<Passenger?>) -> Unit)

    // get passenger; and listen to changes
    fun getPassenger(result: (UiState<Passenger>) -> Unit)

    // store passenger locations
    fun updatePassengerLocations(pickup: LatLng, destination: LatLng, result: (UiState<Boolean>) -> Unit)

    // update status of passenger
    fun updatePassengerState(status: String, result: (UiState<String>) -> Unit)
}