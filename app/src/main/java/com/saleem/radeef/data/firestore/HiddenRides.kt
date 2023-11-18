package com.saleem.radeef.data.firestore

data class HiddenRides(
    var hiddenId: String = "",
    val rideId: String = "",
    val driverId: String = "",
    val passengerId: String = ""
)
