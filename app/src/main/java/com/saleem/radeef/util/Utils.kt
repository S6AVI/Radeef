package com.saleem.radeef.util

import java.util.Locale

// turn a statement into an expression
val <T> T.exhaustive: T
    get() = this

val genders = listOf<String>("Male", "Female", "Unknown")

fun getCountries(): List<String> {
    val locale = Locale.getDefault()

    return Locale.getISOCountries()
        .map { Locale("", it).getDisplayCountry(locale) }

}

enum class UserType() {
    PASSENGER,
    DRIVER
}