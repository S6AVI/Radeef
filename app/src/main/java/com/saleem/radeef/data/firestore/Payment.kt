package com.saleem.radeef.data.firestore

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class Payment(

    val paymentID: String = "",
    val paymentMethodID: String = "",
    val rideID: String = ""

) : Parcelable