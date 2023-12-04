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

    // start authenticating; send OTP code to user
    fun registerDriver(driver: Driver, phone: String, activity: Activity, result: (UiState<String>) -> Unit)

    // update driver info; create a document if it's a new driver
    fun updateDriverInfo(driver: Driver, result: (UiState<Driver>) -> Unit)

    // logout
    fun logout(result: (UiState<String>) -> Unit)

    // check if code matches; and sign in user to the app
    fun signIn(code: String, result: (UiState<Driver>) -> Unit)


    // check if driver is already registered
     fun isRegistered(): Boolean

     // resend code again
    fun resendCode(activity: Activity, result: (UiState<String>) -> Unit)


    // get driver and listen for changes
    fun getDriver(result: (UiState<Driver>) -> Unit)


    // get vehicle and update it
    fun getVehicle(result: (UiState<Vehicle>) -> Unit)
    fun updateVehicle(vehicle: Vehicle, result: (UiState<String>) -> Unit)


    // create, get license and update it

    fun createEmptyLicense(result: (UiState<License>) -> Unit)
    fun getLicense(result: (UiState<License>) -> Unit)
    fun updateLicense(license: License, result: (UiState<String>) -> Unit)

    // upload license image file to Cloud Storage
    suspend fun uploadLicenseFile(fileUrl: Uri, onResult: (UiState<Uri>) -> Unit)

    // upload image file to Cloud Storage; given file name
    suspend fun uploadImage(fileUrl: Uri, name: String, onResult: (UiState<Uri>) -> Unit)

    // update driver data
    fun updateDriver(driver: Driver, result: (UiState<String>) -> Unit)



    // store driver locations
    fun updateDriverLocations(pickup: RadeefLocation, destination: RadeefLocation, result: (UiState<Boolean>) -> Unit)

    // update current location of driver
    fun updateDriverCurrentLocation(pickup: LatLng, result: (UiState<Boolean>) -> Unit)

    // update destination
    fun updateDriverDestination(destination: LatLng, result: (UiState<Boolean>) -> Unit)

    // get driver; for passenger
    fun getDriver(id: String, result: (UiState<DriverWithVehicle?>) -> Unit)

    // get driver when ride is done
    fun getDriverWhenArrived(id: String, result: (UiState<DriverWithVehicle?>) -> Unit)

    // check if phone number is associated already with a passenger
    fun isPhoneNumberAssociatedWithPassenger(phone: String, callback: (Boolean) -> Unit)

}