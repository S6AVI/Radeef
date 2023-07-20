package com.saleem.radeef.data.repository

import android.app.Activity
import com.google.firebase.auth.FirebaseUser

import com.saleem.radeef.data.firestore.Passenger
import com.saleem.radeef.util.UiState

interface AuthRepository {
    //val currentUser: FirebaseUser?

    fun registerPassenger(passenger: Passenger, phone: String, activity: Activity, result: (UiState<String>) -> Unit)

    fun updatePassengerInfo(passenger: Passenger, result: (UiState<String>) -> Unit)

    fun logout(result: (UiState<String>) -> Unit)
    fun signIn(code: String, result: (UiState<String>) -> Unit)

    fun updateName(name:String, result: (UiState<String>) -> Unit)
    abstract fun isRegistered(): Boolean
    fun resendCode(activity: Activity, result: (UiState<String>) -> Unit)
    abstract fun hasName(): Boolean
}