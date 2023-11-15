package com.saleem.radeef.data.repository

import com.saleem.radeef.data.firestore.Passenger
import com.saleem.radeef.util.UiState

interface CloudRepository {

    fun updatePassengerInfo(passenger: Passenger, result: (UiState<String>) -> Unit)
}