package com.saleem.radeef.data.repository

import com.saleem.radeef.data.firestore.Driver
import com.saleem.radeef.util.UiState

interface CloudRepository {

    fun updatePassengerInfo(passenger: Driver, result: (UiState<String>) -> Unit)
}