package com.saleem.radeef.data.firestore

import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ServerTimestamp

import com.google.type.DateTime
import java.util.Date


data class Ride(



    val pickupLocation: GeoPoint = GeoPoint(0.0, 0.0),
    val destination: GeoPoint = GeoPoint(0.0, 0.0),

    val chargeAmount: Double = 0.0,

    @ServerTimestamp
    val startTime: Date = Date(),

    @ServerTimestamp
    val endTime: Date = Date(),



    var rideID: String = "",

    val passengerID: String = "",
    //val driverID: Int
) {

    val pickup = LatLng(pickupLocation.latitude, pickupLocation.longitude)
    val dist = LatLng(destination.latitude, destination.longitude)

}