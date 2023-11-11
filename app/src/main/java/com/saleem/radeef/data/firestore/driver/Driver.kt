package com.saleem.radeef.data.firestore.driver

import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng
import com.saleem.radeef.data.RadeefLocation
import com.saleem.radeef.data.RadeefLocationWrapper
import kotlinx.parcelize.Parcelize
import java.util.Locale

@Parcelize
data class Driver(

    //val vehicleID: String = "",

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

    // # note: just split them into four fields; also, can get approach from training project
//
//    val pickup: RadeefLocationWrapper = RadeefLocationWrapper.NullLocation,
//    val destination: RadeefLocationWrapper = RadeefLocationWrapper.NullLocation

    // foreign keys
//    val licenseID: String = "",



) : Parcelable


enum class Sex(val value: String) {
    MALE("MALE"),
    FEMALE("FEMALE"),
    NOTSPECIFIED("none")
}

enum class UserStatus(val value: String) {
    INACTIVE("INACTIVE"),
    SEARCHING("SEARCHING"),
    IN_RIDE("IN_RIDE")
}


enum class RegistrationStatus(val value: String) {
    INFO("INFO"),
    LICENSE("LICENSE"),
    VEHICLE("VEHICLE"),
    COMPLETED("COMPLETED")
}
