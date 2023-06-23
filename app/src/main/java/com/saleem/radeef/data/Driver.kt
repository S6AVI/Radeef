package com.saleem.radeef.data

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(tableName = "driver_table")
@Parcelize
data class Driver(

    val userName: String,
    val email: String,
    val phoneNumber: String,
    val sex: Char,
    val rating: Int,
    val nationality: String,
    @PrimaryKey(autoGenerate = true)
    val driverID: Int = 0,

    // foreign keys
    val licenseID: Int,
    val vehicleID: Int

) : Parcelable