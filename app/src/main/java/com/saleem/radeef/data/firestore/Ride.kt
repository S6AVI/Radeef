package com.saleem.radeef.data.firestore

import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ServerTimestamp
import com.saleem.radeef.driver.ui.home.RideWithDistance
import kotlinx.parcelize.RawValue

import java.util.Date


data class Ride(
    val passengerPickupLocation: @RawValue GeoPoint = GeoPoint(0.0, 0.0),
    val passengerDestination: @RawValue GeoPoint = GeoPoint(0.0, 0.0),

    val chargeAmount: Double = 0.0,

    @ServerTimestamp
    val startTime: Date = Date(),

    @ServerTimestamp
    val endTime: Date = Date(),

    var rideID: String = "",

    val passengerID: String = "",

    val passengerName: String = "",

    val driverId: String = "",

    val driverName: String = "",

    val driverLocation: @RawValue GeoPoint = GeoPoint(0.0, 0.0),

    val status: String = RideStatus.SEARCHING_FOR_DRIVER.value,

    val distance: Double = 0.0
) {

    val passengerPickupLatLng: LatLng
        get() = LatLng(passengerPickupLocation.latitude, passengerPickupLocation.longitude)
    val passengerDestLatLng: LatLng
    get() = LatLng(passengerDestination.latitude, passengerDestination.longitude)

    val driverLocationLatLng: LatLng
        get() = LatLng(driverLocation.latitude, driverLocation.longitude)

}
enum class RideStatus(val value: String) {
    SEARCHING_FOR_DRIVER("SEARCHING_FOR_DRIVER"),
    WAITING_FOR_CONFIRMATION("WAITING_FOR_CONFIRMATION"),
    PASSENGER_PICK_UP("PASSENGER_PICK_UP"),
    EN_ROUTE("EN_ROUTE"),
    ARRIVED("ARRIVED"),
    //COMPLETED("COMPLETED"),
    CANCELED("CANCELED"),
}