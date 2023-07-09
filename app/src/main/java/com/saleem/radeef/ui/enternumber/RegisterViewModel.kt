package com.saleem.radeef.ui.enternumber

import android.app.Activity
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.PhoneAuthCredential
import com.saleem.radeef.data.firestore.Passenger
import com.saleem.radeef.data.firestore.Ride

import com.saleem.radeef.data.repository.AuthRepository
import com.saleem.radeef.util.UiState


class RegisterViewModel @ViewModelInject constructor(
    val repository: AuthRepository
) : ViewModel() {


    private val _register = MutableLiveData<UiState<String>>()
    val register: LiveData<UiState<String>>
        get() = _register

    private val _verify = MutableLiveData<UiState<String>>()
    val verify: LiveData<UiState<String>>
        get() = _verify

    private val _name = MutableLiveData<UiState<String>>()
    val name: LiveData<UiState<String>>
        get() = _name

    fun register(
        passenger: Passenger,
        phone: String,
        activity: Activity,
        onVerificationCompleted: (Any) -> Unit = { credential ->
           // repository.signIn(passenger, credential) { state ->
              //  _verify.value = state
            }

    ) {
        _register.value = UiState.Loading
        repository.registerPassenger(
            passenger = passenger,
            phone = phone,
            activity = activity
        ) {
            _register.value = it
        }
    }

    fun signInWithPhoneAuthCredential(code: String) {
        _verify.value = UiState.Loading
        repository.signIn(code) { state ->
            _verify.value = state
        }
    }

    fun updateName(name: String) {
        repository.updateName(name) {state ->
            _name.value = state
        }
    }
}