package com.saleem.radeef.driver.ui.profile


import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.saleem.radeef.data.model.Driver
import com.saleem.radeef.data.repository.DriverRepository
import com.saleem.radeef.util.UiState

class DriverProfileViewModel @ViewModelInject constructor(
    private val repository: DriverRepository,
) : ViewModel() {


    private val _driver = MutableLiveData<UiState<Driver>>()
    val driver: LiveData<UiState<Driver>>
        get() = _driver

    private val _update = MutableLiveData<UiState<String>>()
    val update: LiveData<UiState<String>>
        get() = _update

    var driverData: Driver? = null



    fun getDriver() {
        _driver.value = UiState.Loading

        repository.getDriver { state ->
            _driver.value = state

            if (state is UiState.Success) {
                driverData = state.data
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