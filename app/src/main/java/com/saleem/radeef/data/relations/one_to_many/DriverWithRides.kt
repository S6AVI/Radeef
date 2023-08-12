package com.saleem.radeef.data.relations.one_to_many

import androidx.room.Embedded
import androidx.room.Relation
import com.saleem.radeef.data.firestore.driver.Driver
import com.saleem.radeef.data.relations.Ride

data class DriverWithRides(
    @Embedded val driver: Driver,
    @Relation(
        parentColumn = "driverID",
        entityColumn = "driverID"
    )
    val rides: List<Ride>
)
