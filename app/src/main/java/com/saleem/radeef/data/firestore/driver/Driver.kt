package com.saleem.radeef.data.firestore.driver

import android.os.Parcel
import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ServerTimestamp
import com.saleem.radeef.data.RadeefLocation
import com.saleem.radeef.data.RadeefLocationWrapper
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue
import java.util.Locale

@Parcelize
data class Driver(

    val email: String = "",
    val phoneNumber: String = "",
    val sex: String = Sex.NOTSPECIFIED.value,
    val nationality: String = Locale.getDefault().country,
    var driverID: String = "",
    val personalPhotoUrl: String = "",
    val cardPhotoUrl: String = "",
    val name: String = "",

    val identityNumber: String = "",
    val status: String = UserStatus.INACTIVE.value,

    val registrationStatus: String = RegistrationStatus.INFO.value,

    // # note: just split them into four fields;


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


enum class Sex(val value: String) {
    MALE("MALE"),
    FEMALE("FEMALE"),
    NOTSPECIFIED("none")
}

enum class UserStatus(val value: String) {
    INACTIVE("INACTIVE"),
    SEARCHING("SEARCHING"),
    IN_RIDE("IN_RIDE"),
    CONTINUE("CONTINUE")
}


enum class RegistrationStatus(val value: String) {
    INFO("INFO"),
    LICENSE("LICENSE"),
    VEHICLE("VEHICLE"),
    COMPLETED("COMPLETED")
}
