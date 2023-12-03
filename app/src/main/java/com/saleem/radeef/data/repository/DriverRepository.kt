package com.saleem.radeef.data.repository

import android.app.Activity
import android.net.Uri
import com.google.android.gms.maps.model.LatLng
import com.saleem.radeef.data.model.RadeefLocation

import com.saleem.radeef.data.model.Driver
import com.saleem.radeef.data.model.DriverWithVehicle
import com.saleem.radeef.data.model.License
import com.saleem.radeef.data.model.Vehicle
import com.saleem.radeef.util.UiState

interface DriverRepository {
    //val currentUser: FirebaseUser?

    fun registerDriver(driver: Driver, phone: String, activity: Activity, result: (UiState<String>) -> Unit)

    fun updateDriverInfo(driver: Driver, result: (UiState<Driver>) -> Unit)

    fun logout(result: (UiState<String>) -> Unit)
    fun signIn(code: String, result: (UiState<Driver>) -> Unit)

    fun updateName(name:String, result: (UiState<String>) -> Unit)

     fun isRegistered(): Boolean
    fun resendCode(activity: Activity, result: (UiState<String>) -> Unit)
    fun hasName(callback: (Boolean) -> Unit)

    fun getName(result: (UiState<String>) -> Unit)


    fun getDriver(result: (UiState<Driver>) -> Unit)


    fun getVehicle(result: (UiState<Vehicle>) -> Unit)
    fun updateVehicle(vehicle: Vehicle, result: (UiState<String>) -> Unit)


    fun getLicense(result: (UiState<License>) -> Unit)
    fun updateLicense(license: License, result: (UiState<String>) -> Unit)

    suspend fun uploadLicenseFile(fileUrl: Uri, onResult: (UiState<Uri>) -> Unit)

    suspend fun uploadImage(fileUrl: Uri, name: String, onResult: (UiState<Uri>) -> Unit)

    fun updateDriver(driver: Driver, result: (UiState<String>) -> Unit)

    fun alreadyUploaded(uri: Uri): Boolean {
        return uri.toString().contains("radeef-bc315.appspot.com")
    }

    fun createEmptyLicense(result: (UiState<License>) -> Unit)

    fun updateDriverLocations(pickup: RadeefLocation, destination: RadeefLocation, result: (UiState<Boolean>) -> Unit)

    fun updateDriverCurrentLocation(pickup: LatLng, result: (UiState<Boolean>) -> Unit)

    fun updateDriverDestination(destination: LatLng, result: (UiState<Boolean>) -> Unit)
    fun getDriver(id: String, result: (UiState<DriverWithVehicle?>) -> Unit)

    fun getDriverWhenArrived(id: String, result: (UiState<DriverWithVehicle?>) -> Unit)
    fun isPhoneNumberAssociatedWithPassenger(phone: String, callback: (Boolean) -> Unit)

}