package com.saleem.radeef.data

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(tableName = "passenger_table")
@Parcelize
data class Passenger (

    val userName: String,
    val email: String,
    val phoneNumber: String,
    val sex: Char,
    val rating: Int,

    @PrimaryKey(autoGenerate = true)
    val passengerID: Int = 0,


        ): Parcelable