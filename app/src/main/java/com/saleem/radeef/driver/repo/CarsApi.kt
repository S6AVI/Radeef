package com.saleem.radeef.driver.repo

import com.saleem.radeef.R
import com.saleem.radeef.data.CarData
import com.saleem.radeef.driver.CARS_API_KEY


import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

interface CarsApi {
    companion object {
        const val BASE_URL = "https://api.api-ninjas.com"
        const val CLIENT_ID = CARS_API_KEY
    }

    @Headers("X-Api-Key: $CLIENT_ID")
    @GET("/v1/cars")
    suspend fun getCars(
        @Query("model") model: String,
        @Query("make") make: String,
    ): List<CarData>

}