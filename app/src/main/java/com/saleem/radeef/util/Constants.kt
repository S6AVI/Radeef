package com.saleem.radeef.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


const val FEE_RATE = 3
const val FEE_BASE = 0

object FirestoreTables {
    val RIDES = "rides"
    val PASSENGERS = "passengers"
    val DRIVERS = "drivers"
    val VEHICLES = "vehicles"
    val LICENSE = "license"
}

object Constants {

    const val PERMISSION_LOCATION_REQUEST_CODE = 9001
    const val PICK_IMAGE_REQUEST = 9002
    const val CROP_IMAGE_REQUEST = 9003
    //const val PERMISSION_BACKGROUND_LOCATION_REQUEST_CODE = 2

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

object ImageFileNames {
    const val PERSONAL = "personal"
    const val LICENSE = "license"
    const val VEHICLE = "vehicle"
}