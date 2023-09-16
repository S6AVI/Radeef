package com.saleem.radeef.driver.ui.auth

import android.app.Activity
import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.saleem.radeef.data.firestore.driver.Driver
import com.saleem.radeef.driver.repo.DriverRepository
import com.saleem.radeef.util.UiState


class DriverAuthViewModel @ViewModelInject constructor(
    val repository: DriverRepository
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

    private val _logout = MutableLiveData<UiState<String>>()
    val logout: LiveData<UiState<String>>
        get() = _logout

    fun register(
        driver: Driver,
        phone: String,
        activity: Activity,
        onVerificationCompleted: (Any) -> Unit = { credential ->
           // repository.signIn(passenger, credential) { state ->
              //  _verify.value = state
            }

    ) {
        _register.value = UiState.Loading
        repository.registerDriver(
            driver = driver,
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

    fun isRegistered() = repository.isRegistered()

    fun resendCode(activity: Activity) {
        repository.resendCode(activity) {

        }
    }

    fun signOut() {
        Log.d("savii", "inside viewModel")
        repository.logout {
            _logout.value = it
        }
    }
}