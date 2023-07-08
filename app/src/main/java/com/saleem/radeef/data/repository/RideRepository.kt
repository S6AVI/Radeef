package com.saleem.radeef.data.repository

import com.saleem.radeef.data.firestore.Ride
import com.saleem.radeef.util.UiState

interface RideRepository {

    fun getRides(result: (UiState<List<Ride>>) -> Unit)
    fun addRide(ride: Ride, result: (UiState<String>) -> Unit)
}