package com.saleem.radeef.data.firestore

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PaymentMethod(

    val type: String = "",
    val paymentMethodID: String = ""

) : Parcelable