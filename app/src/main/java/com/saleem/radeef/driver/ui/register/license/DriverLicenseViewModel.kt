package com.saleem.radeef.driver.ui.register.license

import android.net.Uri
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.saleem.radeef.data.model.License
import com.saleem.radeef.data.repository.DriverRepository
import com.saleem.radeef.util.UiState
import com.saleem.radeef.util.logD
import kotlinx.coroutines.launch

class DriverLicenseViewModel @ViewModelInject constructor(
    val repository: DriverRepository
) : ViewModel() {


    private val _license = MutableLiveData<UiState<License>>()
    val license: LiveData<UiState<License>>
        get() = _license

    private val _uploadImage = MutableLiveData<UiState<Uri>>()
    val uploadImage: LiveData<UiState<Uri>>
        get() = _uploadImage

    private val _updateLicense = MutableLiveData<UiState<String>>()
    val updateLicense: LiveData<UiState<String>>
        get() = _updateLicense

    var licenseData: License? = null

    init {
        getLicense()
    }

    private fun getLicense() {
        _license.value = UiState.Loading
        repository.getLicense {state ->
            _license.value = state
            logD("ViewModel: in getLicense")
            if (state is UiState.Success) {
                logD("ViewModel: in getLicense: success: ${state.data}")
                licenseData = state.data
            } else {
                logD("some problem in getLicense")
            }
        }

    }

    fun onContinueClicked(imageUri: Uri, name: String) {
        _uploadImage.value = UiState.Loading
        viewModelScope.launch {
            repository.uploadImage(imageUri, name) {
                logD("after uploadImage in ViewModel: $imageUri\n$name")
                _uploadImage.value = it
            }
        }
    }

    fun updateLicenseInfo(license: License) {
        _updateLicense.value = UiState.Loading
        repository.updateLicense(license) {
            logD("after updateLicense in ViewModel: $license")
            _updateLicense.value = it
        }
    }



}
