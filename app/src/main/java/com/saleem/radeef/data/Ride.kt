package com.saleem.radeef.data

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.sql.Time
import java.time.format.DateTimeFormatter

@Entity(tableName = "ride_table")
@Parcelize
data class Ride(

    val pickupLocation: String, // type to be changed
    val destination: String,
    val chargeAmount: Double,
    val startTime: Long,
    val endTime: Long,

    @PrimaryKey(autoGenerate = true)
    val rideID: Int = 0,

    // foreign keys
    val passengerID: Int,
    val driverID: Int
): Parcelable