package com.saleem.radeef.data.model

import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.GeoPoint
import com.saleem.radeef.util.DriverStatus
import com.saleem.radeef.util.RegistrationStatus
import com.saleem.radeef.util.Sex
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

@Parcelize
data class Driver(

    val email: String = "",
    val phoneNumber: String = "",
    val sex: String = Sex.NONE.value,
    val nationality: String = "",
    var driverID: String = "",
    val personalPhotoUrl: String = "",
    val cardPhotoUrl: String = "",
    val name: String = "",

    val identityNumber: String = "",
    val status: String = DriverStatus.INACTIVE.value,

    val registrationStatus: String = RegistrationStatus.INFO.value,

    val pickup: @RawValue GeoPoint =  GeoPoint(0.0, 0.0),
    val destination: @RawValue GeoPoint = GeoPoint(0.0, 0.0),

    val pickup_title: String = "",
    val destination_title: String = ""

) : Parcelable
{

    val pickupLatLng: LatLng
        get()  = LatLng(pickup.latitude, pickup.longitude)

    val destinationLatLng: LatLng
        get()  = LatLng(destination.latitude, destination.longitude)
}