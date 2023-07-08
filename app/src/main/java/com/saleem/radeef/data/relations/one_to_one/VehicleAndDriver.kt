package com.saleem.radeef.data.relations.one_to_one

import androidx.room.Embedded
import androidx.room.Relation
import com.saleem.radeef.data.relations.Driver
import com.saleem.radeef.data.relations.Vehicle


data class VehicleAndDriver(
    @Embedded val vehicle: Vehicle,
    @Relation(
        parentColumn = "vehicleID",
        entityColumn = "vehicleID"
    )
    val driver: Driver
)
