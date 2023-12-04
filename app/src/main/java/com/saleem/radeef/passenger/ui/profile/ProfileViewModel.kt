package com.saleem.radeef.passenger.ui.profile

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.saleem.radeef.data.model.Passenger
import com.saleem.radeef.data.repository.AuthRepository
import com.saleem.radeef.data.repository.CloudRepository
import com.saleem.radeef.util.UiState

class ProfileViewModel @ViewModelInject constructor(
    private val repository: AuthRepository,
    private val cloudRepo: CloudRepository
) : ViewModel() {



    private val _passenger = MutableLiveData<UiState<Passenger>>()
    val passenger: LiveData<UiState<Passenger>>
        get() = _passenger


    private val _update = MutableLiveData<UiState<String>>()
    val update: LiveData<UiState<String>>
        get() = _update


    fun getPassenger() {
        _passenger.value = UiState.Loading
        repository.getPassenger {
            _passenger.value = it
        }
    }

    fun updatePassengerInfo(passenger: Passenger) {
        _update.value = UiState.Loading
        cloudRepo.updatePassengerInfo(passenger) {
            _update.value = it
        }
    }


}