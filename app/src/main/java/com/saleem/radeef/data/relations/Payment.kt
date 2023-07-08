package com.saleem.radeef.data.relations

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize


@Entity(tableName = "payment_table")
@Parcelize
data class Payment (

    @PrimaryKey(autoGenerate = true)
    val paymentID: Int = 0,

    val paymentMethodID: Int,
    val rideID: Int
        ): Parcelable