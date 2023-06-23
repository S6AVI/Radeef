package com.saleem.radeef.data

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.util.Date

@Entity(tableName = "vehicle_table")
@Parcelize
data class Vehicle(

    val numberOfSeats: Int,
    val year: Long,
    val color: String,
    val make: String,
    val model: String,
    val plateNumber: String,
    val weight: Int,
    val serialNumber: Long,

    @PrimaryKey(autoGenerate = true)
    val vehicleID: Int = 0
): Parcelable