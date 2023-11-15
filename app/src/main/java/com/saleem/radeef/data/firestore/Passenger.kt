package com.saleem.radeef.data.firestore

import android.os.Parcelable

import kotlinx.parcelize.Parcelize

@Parcelize
data class Passenger(

    val phoneNumber: String = "",
    val name: String = "",
    var passengerID: String = "",
    val email: String = "",
    val gender: String = ""

) : Parcelable