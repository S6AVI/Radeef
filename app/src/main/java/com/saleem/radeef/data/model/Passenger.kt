package com.saleem.radeef.data.model

import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.GeoPoint
import com.saleem.radeef.util.PassengerStatus
import com.saleem.radeef.util.Gender

import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

/*
data class for Passenger collection
 */
@Parcelize
data class Passenger(

    val phoneNumber: String = "",
    val name: String = "",
    var passengerID: String = "",
    val email: String = "",
    val gender: String = Gender.NONE.value,
    val personalPhotoUrl: String = "",
    val status: String = PassengerStatus.INACTIVE.value,

    val pickup: @RawValue GeoPoint =  GeoPoint(0.0, 0.0),
    val destination: @RawValue GeoPoint = GeoPoint(0.0, 0.0),

    ) : Parcelable
{

    /*
    custom getters to cast GeoPoint into LatLng
     */
    val pickupLatLng: LatLng
        get()  = LatLng(pickup.latitude, pickup.longitude)

    val destinationLatLng: LatLng
        get()  = LatLng(destination.latitude, destination.longitude)
}