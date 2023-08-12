package com.saleem.radeef.data.firestore.driver

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.util.Date


@Parcelize
data class License(

    val dob: Date = Date(),
    val issDate: Date = Date(),
    val expDate: Date = Date(),
    val bloodType: String = BloodType.A_POSITIVE.value,

    val photoUrl: String = "",


    val licenseID: String = "",

    val driverID: String = ""
) : Parcelable

enum class BloodType(val value: String) {
    A_POSITIVE("A+"),
    A_NEGATIVE("A-"),
    B_POSITIVE("B+"),
    B_NEGATIVE("B-"),
    AB_POSITIVE("AB+"),
    AB_NEGATIVE("AB-"),
    O_POSITIVE("O+"),
    O_NEGATIVE("O-")
}