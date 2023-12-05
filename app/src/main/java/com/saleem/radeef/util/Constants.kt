package com.saleem.radeef.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


const val FEE_RATE = 3
const val FEE_BASE = 0

const val MAX_DISTANCE_METERS_THRESHOLD = 5000

const val MIN_UPDATE_DISTANCE_METERS = 500.0f

const val DELAY = 1000L
object FirestoreTables {
    const val RIDES = "rides"
    const val PASSENGERS = "passengers"
    const val DRIVERS = "drivers"
    const val VEHICLES = "vehicles"
    const val LICENSE = "license"
    const val HIDDEN_RIDES = "hidden_rides"
}

object Constants {

    const val PERMISSION_LOCATION_REQUEST_CODE = 9001
    const val USER_TYPE = "userType"
    const val CURRENT_SCREEN = "currentScreen"
}

object FirebaseStorageConstants {
    const val ROOT_DIRECTORY = "app"
    const val DRIVER_DIRECTORY = "driver"
    const val Passenger_DIRECTORY = "passenger"
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

const val TAG = "savii"