package com.saleem.radeef.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CarData(
    val make: String,
    val model: String,
): Parcelable