package com.saleem.radeef.driver.ui.register.info

import android.net.Uri
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
//import com.saleem.radeef.data.firestore.Driver
import com.saleem.radeef.data.firestore.driver.Driver
import com.saleem.radeef.driver.repo.DriverRepository
import com.saleem.radeef.util.UiState
import kotlinx.coroutines.launch

class DriverInfoViewModel @ViewModelInject constructor(
    val repository: DriverRepository
) : ViewModel() {


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
        getDriver()
    }

    private fun getDriver() {
        _driver.value = UiState.Loading
        repository.getDriver {
            _driver.value = it
        }
    }

    fun onContinueClicked(imageUri: Uri, name: String) {
        _uploadImage.value = UiState.Loading
        viewModelScope.launch {
            repository.uploadImage(imageUri, name) {
                _uploadImage.value = it
            }
        }
    }

    fun updateDriverInfo(driver: Driver) {
        _updateDriver.value = UiState.Loading
        repository.updateDriver(driver) {
            _updateDriver.value = it
        }
    }


}
