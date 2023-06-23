package com.saleem.radeef.data.relations.one_to_one

import androidx.room.Embedded
import androidx.room.Relation
import com.saleem.radeef.data.Payment
import com.saleem.radeef.data.Ride

data class RideAndPayment(
    @Embedded val ride: Ride,
    @Relation(
        parentColumn = "rideID",
        entityColumn = "rideID"
    )
    val payment: Payment
)
