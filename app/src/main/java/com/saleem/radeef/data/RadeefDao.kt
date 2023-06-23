package com.saleem.radeef.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy

@Dao
interface RadeefDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(passenger: Passenger)


}