package com.saleem.radeef.data.model

import android.os.Parcelable
import com.saleem.radeef.util.BloodType
import com.saleem.radeef.util.DefaultDate.DEFAULT_DATE
import kotlinx.parcelize.Parcelize
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@Parcelize
data class License(

    val dob: Date = Date(),
    val issDate: Date = DEFAULT_DATE,
    val expDate: Date = DEFAULT_DATE,
    val bloodType: String = BloodType.A_POSITIVE.value,

    val photoUrl: String = "",


    val licenseID: String = "",

    val driverID: String = ""
) : Parcelable {
    val formattedIssDate: String
        get() = if (issDate == DEFAULT_DATE) "" else formatDate(issDate)

    val formattedExpDate: String
        get() = if (expDate == DEFAULT_DATE) "" else formatDate(expDate)

    private fun formatDate(date: Date): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(date)
    }
}

