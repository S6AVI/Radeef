package com.saleem.radeef.data.relations.one_to_one

import androidx.room.Embedded
import androidx.room.Relation
import com.saleem.radeef.data.firestore.driver.Driver
import com.saleem.radeef.data.firestore.driver.License


data class LicenseAndDriver(
    @Embedded val license: License,
    @Relation(
        parentColumn = "licenseID",
        entityColumn = "licenseID"
    )
    val driver: Driver

)
