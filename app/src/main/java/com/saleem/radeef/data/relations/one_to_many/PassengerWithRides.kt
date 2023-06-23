package com.saleem.radeef.data.relations.one_to_many

import androidx.room.Embedded
import androidx.room.Relation
import com.saleem.radeef.data.Passenger
import com.saleem.radeef.data.Ride

data class PassengerWithRides(
    @Embedded val passenger: Passenger,
    @Relation(
        parentColumn = "passengerID",
        entityColumn = "passengerID"
    )
    val rides: List<Ride>
)
