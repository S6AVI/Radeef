package com.saleem.radeef.data.relations

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.saleem.radeef.data.firestore.driver.Driver

//@Dao
//interface RadeefDao {
//
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    suspend fun insert(passenger: Passenger)
//
//    @Update
//    suspend fun update(passenger: Passenger)
//
//    @Delete
//    suspend fun delete(passenger: Passenger)
//
//
//    @Query("SELECT * FROM passenger_table WHERE passengerID = :passengerID")
//    suspend fun getPassenger(passengerID: Int): Passenger
//
//
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    suspend fun insert(driver: Driver)
//
//    @Update
//    suspend fun update(driver: Driver)
//
//    @Delete
//    suspend fun delete(driver: Driver)
//
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    suspend fun insert(ride: Ride)
//
//    @Update
//    suspend fun update(ride: Ride)
//
//    @Delete
//    suspend fun delete(ride: Ride)
//}