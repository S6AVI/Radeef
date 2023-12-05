package com.saleem.radeef.util

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import com.saleem.radeef.data.model.Ride
import com.saleem.radeef.util.Constants.CURRENT_SCREEN
import java.io.File
import java.util.Calendar
import java.util.Locale

// turn a statement into an expression
val <T> T.exhaustive: T
    get() = this

val genders = listOf("Male", "Female", "Unknown")

val bloodTypes = listOf(
    "A+",
    "A-",
    "B+",
    "B-",
    "AB+",
    "AB-",
    "O+",
    "O-",
)

val carColors = listOf(
    "Black",
    "White",
    "Silver",
    "Gray",
    "Red",
    "Blue",
    "Green",
    "Yellow",
    "Orange",
    "Brown",
    "Beige",
    "Gold",
    "Purple",
    "Pink",
    "Turquoise",
    "Bronze",
    "Copper",
    "Magenta",
    "Lavender"
)

val carCapacities = (2..8).toList()




fun getCountries(): List<String> {
    val locale = Locale.getDefault()

    return Locale.getISOCountries()
        .map { Locale("", it).getDisplayCountry(locale) }

}

fun getYears(): List<Int> {

    val years = mutableListOf<Int>()
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)

    for (year in 2010..currentYear) {
        years.add(year)
    }

    return years.reversed()
}



enum class UserType {
    PASSENGER,
    DRIVER
}


fun updateRegistrationStatus(status: RegistrationStatus, activity: Activity) {
    val preferences: SharedPreferences =
        activity.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)

    preferences.edit().putString(CURRENT_SCREEN, status.value).apply()
}

fun updateRegistrationStatus(status: String, activity: Activity) {
    val preferences: SharedPreferences =
        activity.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)

    preferences.edit().putString(CURRENT_SCREEN, status).apply()


}

fun isPassed(preferences: SharedPreferences, current: String): Boolean {
    return preferences.getString(CURRENT_SCREEN, null) != current
}

fun calculateFee(distance: Double): Double {
 return FEE_BASE + (distance * FEE_RATE)
}
fun renameImageFile(fileUrl: Uri): Uri {
    val originalFile = File(fileUrl.path!!)
    val renamedFile = File.createTempFile("image_", ".jpg")

    originalFile.copyTo(renamedFile, true)
    return Uri.fromFile(renamedFile)
}



fun alreadyUploaded(uri: Uri): Boolean {
    return uri.toString().contains("radeef-bc315.appspot.com")
}


data class RideWithDistance(val ride: Ride, val distance: Double)