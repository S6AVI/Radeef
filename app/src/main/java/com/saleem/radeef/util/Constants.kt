package com.saleem.radeef.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


const val FEE_RATE = 3
const val FEE_BASE = 0

const val MAX_DISTANCE_METERS_THRESHOLD = 5000

const val MIN_UPDATE_DISTANCE_METERS = 500.0f

object FirestoreTables {
    val RIDES = "rides"
    val PASSENGERS = "passengers"
    val DRIVERS = "drivers"
    val VEHICLES = "vehicles"
    val LICENSE = "license"
    val HIDDEN_RIDES = "hidden_rides"
}

object Constants {

    const val PERMISSION_LOCATION_REQUEST_CODE = 9001
    const val USER_TYPE = "userType"
    const val CURRENT_SCREEN = "currentScreen"
}

object FirebaseStorageConstants {
    val ROOT_DIRECTORY = "app"
    val DRIVER_DIRECTORY = "driver"
    val Passenger_DIRECTORY = "passenger"
}

object DefaultDate {
    val DEFAULT_DATE: Date
        get() = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).parse("01-01-1900")!!
}

const val GOOGLE_MAPS_KEY = "AIzaSyC2RlRiGhgV_sKcjfNky9qwfS4B0AOUrDE"

object ImageFileNames {
    const val PERSONAL = "personal"
    const val LICENSE = "license"
    const val VEHICLE = "vehicle"
}

val TAG = "savii"