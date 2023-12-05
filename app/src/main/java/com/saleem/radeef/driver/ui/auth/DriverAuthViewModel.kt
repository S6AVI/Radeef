package com.saleem.radeef.driver.ui.auth

import android.app.Activity
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.saleem.radeef.data.model.Driver
import com.saleem.radeef.data.repository.DriverRepository
import com.saleem.radeef.util.UiState


class DriverAuthViewModel @ViewModelInject constructor(
    val repository: DriverRepository
) : ViewModel() {


    private val _register = MutableLiveData<UiState<String>>()
    val register: LiveData<UiState<String>>
        get() = _register

    private val _verify = MutableLiveData<UiState<Driver>>()
    val verify: LiveData<UiState<Driver>>
        get() = _verify


    private val _logout = MutableLiveData<UiState<String>>()
    val logout: LiveData<UiState<String>>
        get() = _logout

    fun register(driver: Driver, phone: String, activity: Activity) {
        _register.value = UiState.Loading
        repository.isPhoneNumberAssociatedWithPassenger(phone) { result ->
            if (result) {
                _register.value =
                    UiState.Failure("Phone number: $phone is already associated with a passenger!")
            } else {
                repository.registerDriver(
                    driver = driver, phone = phone, activity = activity
                ) {
                    _register.value = it
                }
            }
        }

    }

    fun signInWithPhoneAuthCredential(code: String) {
        _verify.value = UiState.Loading
        repository.verifyCode(code) { state ->
            _verify.value = state
        }
    }



    fun isRegistered() = repository.isRegistered()

    fun resendCode(activity: Activity) {
        repository.resendCode(activity) {

        }
    }

    fun signOut() {
        repository.logout {
            _logout.value = it
        }
    }
}