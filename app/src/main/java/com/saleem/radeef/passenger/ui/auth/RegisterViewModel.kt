package com.saleem.radeef.passenger.ui.auth

import android.app.Activity
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.saleem.radeef.data.model.Passenger

import com.saleem.radeef.data.repository.AuthRepository
import com.saleem.radeef.util.UiState


class RegisterViewModel @ViewModelInject constructor(
    val repository: AuthRepository
) : ViewModel() {


    private val _register = MutableLiveData<UiState<String>>()
    val register: LiveData<UiState<String>>
        get() = _register

    private val _verify = MutableLiveData<UiState<Passenger>>()
    val verify: LiveData<UiState<Passenger>>
        get() = _verify

    private val _name = MutableLiveData<UiState<String>>()
    val name: LiveData<UiState<String>>
        get() = _name

    private val _logout = MutableLiveData<UiState<String>>()
    val logout: LiveData<UiState<String>>
        get() = _logout

    fun register(passenger: Passenger, phone: String, activity: Activity) {
        _register.value = UiState.Loading
        repository.isPhoneNumberAssociatedWithDriver(phone) { result ->
            if (result) {
                _register.value =
                    UiState.Failure("Phone number: $phone is already associated with a driver!")
            } else {
                repository.registerPassenger(
                    passenger = passenger,
                    phone = phone,
                    activity = activity
                ) {
                    _register.value = it
                }
            }
        }
    }


    fun signInWithPhoneAuthCredential(code: String) {
        _verify.value = UiState.Loading
        repository.signIn(code) { state ->
            _verify.value = state
        }
    }

    fun updateName(name: String) {
        _name.value = UiState.Loading
        repository.updateName(name) { state ->
            _name.value = state
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

    fun alreadyHasName(callback: (Boolean) -> Unit) {
        repository.hasName(callback)
    }
}