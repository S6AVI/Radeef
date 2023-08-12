package com.saleem.radeef.data.relations

import androidx.room.Database
import androidx.room.RoomDatabase
import com.saleem.radeef.data.firestore.driver.Driver
import com.saleem.radeef.data.firestore.driver.License
import com.saleem.radeef.data.firestore.driver.Vehicle

//@Database(
//    entities = [Driver::class, Passenger::class, License::class,
//        Payment::class, Ride::class, PaymentMethod::class,
//        Vehicle::class, Admin::class], version = 1)
//abstract class RadeefDatabase: RoomDatabase() {
//    abstract fun radeefDao(): RadeefDao
//}