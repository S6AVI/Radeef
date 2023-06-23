package com.saleem.radeef.data

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.util.Date


@Entity(tableName = "license_table")
@Parcelize
data class License (

    val dob: Long,
    val issDate: Long,
    val expDate: Long,
    val bloodType: String,

    @PrimaryKey(autoGenerate = true)
    val licenseID: Int = 0

        ): Parcelable