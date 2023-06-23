package com.saleem.radeef.data

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(tableName = "payment_method_table")
@Parcelize
data class PaymentMethod (

    val type: String,

    @PrimaryKey(autoGenerate = true)
    val paymentMethodID: Int = 0
        ): Parcelable