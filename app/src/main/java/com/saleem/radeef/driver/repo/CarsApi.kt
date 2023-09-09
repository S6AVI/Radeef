package com.saleem.radeef.driver.repo

import com.saleem.radeef.R
import com.saleem.radeef.data.CarData
import com.saleem.radeef.driver.CARS_API_KEY


import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path
import retrofit2.http.Query

interface CarsApi {
    companion object {
        //const val BASE_URL = "https://vpic.nhtsa.dot.gov/api/vehicles/"
        const val BASE_URL = "https://vpic.nhtsa.dot.gov/api/vehicles/"
        //const val CLIENT_ID = CARS_API_KEY
    }

//    @GET("GetAllMakes?format=json")
//    suspend fun getAllMakes(): MakesResponse

    @GET("GetMakesForVehicleType/car?format=json")
    suspend fun getAllMakes(): MakesResponse


    //@GET("GetModelsForMake/honda?format=json")
    @GET("GetModelsForMake/{make}?format=json")
    suspend fun getModelsForMake(
        @Path("make") make: String
    ): ModelsResponse

}