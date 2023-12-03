package com.saleem.radeef.driver.ui.profile

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.saleem.radeef.data.model.Driver
import com.saleem.radeef.data.repository.CloudRepository
import com.saleem.radeef.data.repository.DriverRepository
import com.saleem.radeef.util.UiState
import com.saleem.radeef.util.logD

class DriverProfileViewModel @ViewModelInject constructor(
    private val repository: DriverRepository,
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


    private val _driver = MutableLiveData<UiState<Driver>>()
    val driver: LiveData<UiState<Driver>>
        get() = _driver

    private val _update = MutableLiveData<UiState<String>>()
    val update: LiveData<UiState<String>>
        get() = _update

    var driverData: Driver? = null


//    fun fetchDriverName() {
//        _name.value = UiState.Loading
//        repository.getName {
//            _name.value = it
//        }
//    }

    fun getDriver() {
        _driver.value = UiState.Loading

        repository.getDriver { state ->
            _driver.value = state

            if (state is UiState.Success) {
                logD("ViewModel: in getLicense: success: ${state.data}")
                driverData = state.data
            } else {
                logD("some problem in getLicense")
            }
        }
    }

    fun updateDriverInfo(driver: Driver) {
        _update.value = UiState.Loading
        repository.updateDriver(driver = driver) {
            _update.value = it
        }
    }


}