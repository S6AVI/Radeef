package com.saleem.radeef.util

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.saleem.radeef.passenger.ui.home.TAG
import com.saleem.radeef.util.Constants.CURRENT_SCREEN
import java.util.Calendar
import java.util.Locale

// turn a statement into an expression
val <T> T.exhaustive: T
    get() = this

val genders = listOf<String>("Male", "Female", "Unknown")

val bloodTypes = listOf<String>(
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



enum class UserType() {
    PASSENGER,
    DRIVER
}

fun String.isValidEmail(): Boolean {
    val emailRegex = Regex("^\\w+([.-]?\\w+)*@\\w+([.-]?\\w+)*(\\.\\w{2,3})+\$")
    return emailRegex.matches(this)
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
    Log.d(TAG, "pref , current: $current \n${preferences.getString(CURRENT_SCREEN, null)}")
    return preferences.getString(CURRENT_SCREEN, null) != current
}

fun calculateFee(distance: Double): Double {
    Log.d(TAG, "calculateFee: $distance")
 return FEE_BASE + (distance * FEE_RATE)
}


