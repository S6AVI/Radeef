package com.saleem.radeef.data.model

import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ServerTimestamp
import com.saleem.radeef.util.RideStatus
import kotlinx.parcelize.RawValue

import java.util.Date


/*
data class for Ride collection
 */
data class Ride(
    val passengerPickupLocation: @RawValue GeoPoint = GeoPoint(0.0, 0.0),
    val passengerDestination: @RawValue GeoPoint = GeoPoint(0.0, 0.0),

    val chargeAmount: Double = 0.0,

    @ServerTimestamp
    val startTime: Date = Date(0),

    @ServerTimestamp
    val endTime: Date = Date(0),

    var rideID: String = "",

    val passengerID: String = "",

    val passengerName: String = "",

    val driverId: String = "",

    val driverName: String = "",

    val driverLocation: @RawValue GeoPoint = GeoPoint(0.0, 0.0),

    val status: String = RideStatus.SEARCHING_FOR_DRIVER.value,

    val distance: Double = 0.0
) {


    /*
    custom getters to cast GeoPoint into LatLng
     */

    val passengerPickupLatLng: LatLng
        get() = LatLng(passengerPickupLocation.latitude, passengerPickupLocation.longitude)
    val passengerDestLatLng: LatLng
    get() = LatLng(passengerDestination.latitude, passengerDestination.longitude)

    val driverLocationLatLng: LatLng
        get() = LatLng(driverLocation.latitude, driverLocation.longitude)

}
