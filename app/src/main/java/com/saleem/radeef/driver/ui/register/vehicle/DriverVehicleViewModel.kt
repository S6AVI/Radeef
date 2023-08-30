package com.saleem.radeef.driver.ui.register.vehicle

import android.net.Uri
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saleem.radeef.data.CarData
import com.saleem.radeef.data.firestore.driver.Driver
import com.saleem.radeef.driver.repo.CarsRepository
import com.saleem.radeef.driver.repo.DriverRepository
import com.saleem.radeef.util.UiState
import com.saleem.radeef.util.logD
import kotlinx.coroutines.launch

class DriverVehicleViewModel @ViewModelInject constructor(
    val driverRepository: DriverRepository,
    val carsRepository: CarsRepository
) : ViewModel() {


    private val _carsData = MutableLiveData<List<CarData>>()
    val carsData: LiveData<List<CarData>>
        get() = _carsData

    fun fetchCars(model: String, make: String) {
        viewModelScope.launch {
            try {
                val cars = carsRepository.getCars(make = make, model = model)
                _carsData.value = cars
                logD("successful fetch")
            } catch (e: Exception) {
                logD(e.message.toString())
            }
        }
    }

    private val _driver = MutableLiveData<UiState<Driver>>()
    val driver: LiveData<UiState<Driver>>
        get() = _driver

    private val _uploadImage = MutableLiveData<UiState<Uri>>()
    val uploadImage: LiveData<UiState<Uri>>
        get() = _uploadImage

    private val _updateDriver = MutableLiveData<UiState<String>>()
    val updateDriver: LiveData<UiState<String>>
        get() = _updateDriver

    init {
        fetchCars("", "A")
        //getLicense()
    }

    private fun getLicense() {

    }

    fun onContinueClicked(imageUri: Uri, name: String) {
//        _uploadImage.value = UiState.Loading
//        viewModelScope.launch {
//            repository.uploadImage(imageUri, name) {
//                _uploadImage.value = it
//            }
//        }
    }

    fun updateDriverInfo(driver: Driver) {
//        _updateDriver.value = UiState.Loading
//        repository.updateDriverInfo(driver) {
//            _updateDriver.value = it
//        }
    }


}
