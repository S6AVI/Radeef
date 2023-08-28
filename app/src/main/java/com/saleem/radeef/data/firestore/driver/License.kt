package com.saleem.radeef.data.firestore.driver

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.saleem.radeef.util.DefaultDate
import com.saleem.radeef.util.DefaultDate.DEFAULT_DATE
import kotlinx.parcelize.Parcelize
import java.text.SimpleDateFormat
import java.util.Calendar
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

enum class BloodType(val value: String) {
    A_POSITIVE("A+"),
    A_NEGATIVE("A-"),
    B_POSITIVE("B+"),
    B_NEGATIVE("B-"),
    AB_POSITIVE("AB+"),
    AB_NEGATIVE("AB-"),
    O_POSITIVE("O+"),
    O_NEGATIVE("O-")
}