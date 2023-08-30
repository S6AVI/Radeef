package com.saleem.radeef.driver.repo

import com.saleem.radeef.data.CarData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CarsRepository @Inject constructor(
    private val carsApi: CarsApi
) {

    suspend fun getCars(model: String, make: String): List<CarData> {
        return carsApi.getCars(make = make, model = model)
    }
}