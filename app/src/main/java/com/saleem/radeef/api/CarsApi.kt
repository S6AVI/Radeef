package com.saleem.radeef.api

import com.saleem.radeef.data.model.MakesResponse
import com.saleem.radeef.data.model.ModelsResponse


import retrofit2.http.GET
import retrofit2.http.Path

interface CarsApi {
    companion object {
        const val BASE_URL = "https://vpic.nhtsa.dot.gov/api/vehicles/"
    }

    @GET("GetMakesForVehicleType/car?format=json")
    suspend fun getAllMakes(): MakesResponse

    @GET("GetModelsForMake/{make}?format=json")
    suspend fun getModelsForMake(
        @Path("make") make: String
    ): ModelsResponse

}