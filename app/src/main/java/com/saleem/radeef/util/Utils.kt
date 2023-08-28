package com.saleem.radeef.util

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.saleem.radeef.data.firestore.driver.RegistrationStatus
import com.saleem.radeef.ui.map.TAG
import com.saleem.radeef.util.Constants.CURRENT_SCREEN
import java.util.Locale
import kotlin.math.log

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

fun getCountries(): List<String> {
    val locale = Locale.getDefault()

    return Locale.getISOCountries()
        .map { Locale("", it).getDisplayCountry(locale) }

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

fun isPassed(preferences: SharedPreferences, current: String): Boolean {
    Log.d(TAG, "pref , current: $current \n${preferences.getString(CURRENT_SCREEN, null)}")
    return preferences.getString(CURRENT_SCREEN, null) != current
}

