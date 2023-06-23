package com.saleem.radeef.data

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize


@Entity(tableName = "admin_table")

@Parcelize
data class Admin (
     val username: String,
     val email: String,

     @PrimaryKey(autoGenerate = true)
     val adminID: Int = 0
): Parcelable