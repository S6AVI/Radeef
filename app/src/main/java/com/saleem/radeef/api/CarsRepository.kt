package com.saleem.radeef.api

import com.saleem.radeef.util.UiState
import com.saleem.radeef.util.logD
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CarsRepository @Inject constructor(
    private val carsApi: CarsApi
) {

    suspend fun getAllMakes(): List<String> {
        val response = carsApi.getAllMakes()
        return response.Results.map { it.name }
    }

    suspend fun getModelsForMake(make: String): UiState<List<String>> {
        try {
            val response = carsApi.getModelsForMake(make)
            logD(response.Results.toString())
            return if (response.Results.isEmpty()) {
                UiState.Failure("no car models")
            } else {
                UiState.Success(response.Results.map { it.name })
            }

        } catch (e: Exception) {
           return UiState.Failure(e.message)
        }

    }
}