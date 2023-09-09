package com.saleem.radeef.driver.ui.register.vehicle

import android.net.Uri
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saleem.radeef.data.firestore.driver.Driver
import com.saleem.radeef.data.firestore.driver.Vehicle
import com.saleem.radeef.driver.repo.CarsRepository
import com.saleem.radeef.driver.repo.DriverRepository
import com.saleem.radeef.util.UiState
import com.saleem.radeef.util.logD
import kotlinx.coroutines.launch

class DriverVehicleViewModel @ViewModelInject constructor(
    val driverRepository: DriverRepository,
    private val carsRepository: CarsRepository
) : ViewModel() {


    private val _makesData = MutableLiveData<List<String>>()
    val makesData: LiveData<List<String>>
        get() = _makesData

    private val _modelsData = MutableLiveData<UiState<List<String>>>()
    val modelsData: LiveData<UiState<List<String>>>
        get() = _modelsData

    private fun fetchMakes() {
        viewModelScope.launch {
            try {
                val makes = carsRepository.getAllMakes()
                _makesData.value = makes
                logD("Successful fetch of makes.")
            } catch (e: Exception) {
                logD(e.message.toString())
            }
        }
    }

    fun fetchModels(make: String) {
        _modelsData.value = UiState.Loading
        viewModelScope.launch {
            _modelsData.value = carsRepository.getModelsForMake(make)
        }
    }


    private val _vehicle = MutableLiveData<UiState<Vehicle>>()
    val vehicle: LiveData<UiState<Vehicle>>
        get() = _vehicle

    private val _uploadImage = MutableLiveData<UiState<Uri>>()
    val uploadImage: LiveData<UiState<Uri>>
        get() = _uploadImage

    private val _updateVehicle = MutableLiveData<UiState<String>>()
    val updateVehicle: LiveData<UiState<String>>
        get() = _updateVehicle


    var vehicleData: Vehicle? = null

    init {
        getVehicle()
    }

    private fun getVehicle() {
        _vehicle.value = UiState.Loading
        driverRepository.getVehicle {state ->
            _vehicle.value = state
            logD("ViewModel: in getLicense")
            if (state is UiState.Success) {
                fetchMakes()
                logD("ViewModel: in getLicense: success: ${state.data}")
                vehicleData = state.data
            } else {
                logD("some problem in getLicense")
            }
        }
    }

    fun onContinueClicked(imageUri: Uri, name: String) {
        _uploadImage.value = UiState.Loading
        viewModelScope.launch {
            driverRepository.uploadImage(imageUri, name) {
                _uploadImage.value = it
            }
        }
    }

    fun updateVehicleInfo(vehicle: Vehicle) {
        _updateVehicle.value = UiState.Loading
        driverRepository.updateVehicle(vehicle) {
            _updateVehicle.value = it
        }
    }



}
