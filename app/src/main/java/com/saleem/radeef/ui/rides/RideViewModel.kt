package com.saleem.radeef.ui.rides

import android.os.Looper
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.saleem.radeef.data.firestore.Ride
import com.saleem.radeef.data.repository.RideRepository
import com.saleem.radeef.util.UiState


class RideViewModel @ViewModelInject constructor(
    val repository: RideRepository
) : ViewModel() {

    private val _rides = MutableLiveData<UiState<List<Ride>>>()
    val rides: LiveData<UiState<List<Ride>>>
        get() = _rides

    private val _addRide = MutableLiveData<UiState<String>>()
    val addRide: LiveData<UiState<String>>
        get() = _addRide

    fun getRides() {
        _rides.value = UiState.Loading
        repository.getRides {
            _rides.value = it
        }

    }

    fun addRide(ride: Ride) {
        _addRide.value = UiState.Loading
        repository.addRide(ride) {
            _addRide.value = it
        }
    }
}