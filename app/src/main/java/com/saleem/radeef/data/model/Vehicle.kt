package com.saleem.radeef.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


/*
data class for Vehicle collection
 */
@Parcelize
data class Vehicle(

    val numberOfSeats: Int = 0,
    val year: Int = 0,
    val color: String = "",
    val make: String = "",
    val model: String = "",
    val plateNumber: String = "",
    val photoUrl: String = "",

    val vehicleID: String = "",

    val driverID: String = ""

): Parcelable