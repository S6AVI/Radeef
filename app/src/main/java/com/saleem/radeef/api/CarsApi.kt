package com.saleem.radeef.api

import com.saleem.radeef.data.model.MakesResponse
import com.saleem.radeef.data.model.ModelsResponse


import retrofit2.http.GET
import retrofit2.http.Path

/*
interface for HTTP requests using Retrofit
 */
interface CarsApi {
    companion object {
        const val BASE_URL = "https://vpic.nhtsa.dot.gov/api/vehicles/"
    }

    // get all car makes
    @GET("GetMakesForVehicleType/car?format=json")
    suspend fun getAllMakes(): MakesResponse

    // get all car models of a given make
    @GET("GetModelsForMake/{make}?format=json")
    suspend fun getModelsForMake(
        @Path("make") make: String
    ): ModelsResponse

}