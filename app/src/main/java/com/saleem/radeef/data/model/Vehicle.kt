package com.saleem.radeef.data.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.util.Date


@Parcelize
data class Vehicle(

    val numberOfSeats: Int = 0,
    val year: Int = 0,
    val color: String = "",
    val make: String = "",
    val model: String = "",
    val plateNumber: String = "",
    val weight: Int = 2,
    val serialNumber: String = "",

    val photoUrl: String = "",

    val vehicleID: String = "",

    val driverID: String = ""

): Parcelable