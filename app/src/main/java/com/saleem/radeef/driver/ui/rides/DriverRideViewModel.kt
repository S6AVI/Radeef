package com.saleem.radeef.driver.ui.rides

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.saleem.radeef.data.model.Ride
import com.saleem.radeef.data.repository.RideRepository
import com.saleem.radeef.util.UiState


class DriverRideViewModel @ViewModelInject constructor(
    val repository: RideRepository
) : ViewModel() {

    private val _rides = MutableLiveData<UiState<List<Ride>>>()
    val rides: LiveData<UiState<List<Ride>>>
        get() = _rides

    fun getRides() {
        _rides.value = UiState.Loading
        repository.getDriverRides {
            _rides.value = it
        }

    }
}