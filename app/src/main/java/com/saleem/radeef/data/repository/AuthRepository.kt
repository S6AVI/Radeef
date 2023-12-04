package com.saleem.radeef.data.repository

import android.app.Activity

import com.saleem.radeef.data.model.Passenger
import com.saleem.radeef.util.UiState

interface AuthRepository {


    // start authenticating; send OTP code to user
    fun registerPassenger(passenger: Passenger, phone: String, activity: Activity, result: (UiState<String>) -> Unit)

    // update passenger info; create a document if it's a new passenger (for auth)
    fun updatePassengerInfo(passenger: Passenger, result: (UiState<Passenger>) -> Unit)

    // logout
    fun logout(result: (UiState<String>) -> Unit)

    // check if code matches; and sign in user to the app
    fun signIn(code: String, result: (UiState<Passenger>) -> Unit)

    // store name of passenger
    fun updateName(name:String, result: (UiState<String>) -> Unit)

    // check if driver is already registered
     fun isRegistered(): Boolean

    // resend code again
    fun resendCode(activity: Activity, result: (UiState<String>) -> Unit)

    // check if passenger has name already (registered)
     fun hasName(callback: (Boolean) -> Unit)

     // fetch name of passenger
     fun getName(result: (UiState<String>) -> Unit)


     // get passenger; listen for changes
     fun getPassenger(result: (UiState<Passenger>) -> Unit)

    // check if phone number is associated already with a driver
    fun isPhoneNumberAssociatedWithDriver(phone: String, callback: (Boolean) -> Unit)

}