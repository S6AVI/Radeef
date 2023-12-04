package com.saleem.radeef.api

import com.saleem.radeef.util.UiState
import javax.inject.Inject
import javax.inject.Singleton

/*
    repository to call Retrofit requests and transform results into lists of String
 */
@Singleton
class CarsRepository @Inject constructor(
    private val carsApi: CarsApi
) {


    // get all car makes
    suspend fun getAllMakes(): List<String> {
        val response = carsApi.getAllMakes()
        return response.Results.map { it.name }
    }

    // get all car models of a given make
    suspend fun getModelsForMake(make: String): UiState<List<String>> {
        return try {
            val response = carsApi.getModelsForMake(make)

            if (response.Results.isEmpty()) {
                UiState.Failure("no car models")
            } else {
                UiState.Success(response.Results.map { it.name })
            }

        } catch (e: Exception) {
            UiState.Failure(e.message)
        }

    }
}