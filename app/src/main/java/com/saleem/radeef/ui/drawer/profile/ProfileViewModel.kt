package com.saleem.radeef.ui.drawer.profile

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.saleem.radeef.data.firestore.Driver
import com.saleem.radeef.data.repository.AuthRepository
import com.saleem.radeef.data.repository.CloudRepository
import com.saleem.radeef.util.UiState

class ProfileViewModel @ViewModelInject constructor(
    private val repository: AuthRepository,
    @Assisted private val state: SavedStateHandle,
    private val cloudRepo: CloudRepository
) : ViewModel() {


//    val passenger = state.get<Passenger>("passenger")
//
//    var passengerName = fetchPassengerName()
//        set(value) {
//            field = value
//            state.set("passengerName", value)
//        }

    private val _name = MutableLiveData<UiState<String>>()
    val name: LiveData<UiState<String>>
        get() = _name


    private val _passenger = MutableLiveData<UiState<Driver>>()
    val passenger: LiveData<UiState<Driver>>
        get() = _passenger

    private val _update = MutableLiveData<UiState<String>>()
    val update: LiveData<UiState<String>>
        get() = _update





    fun fetchPassengerName() {
        _name.value = UiState.Loading
        repository.getName {
            _name.value = it
        }
    }

    fun getPassenger() {
        _passenger.value = UiState.Loading
        repository.getPassenger {
            _passenger.value = it
        }
    }

    fun updatePassengerInfo(passenger: Driver) {
        _update.value = UiState.Loading
        cloudRepo.updatePassengerInfo(passenger) {
            _update.value = it
        }
    }


}