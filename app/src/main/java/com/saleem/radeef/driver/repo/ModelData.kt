package com.saleem.radeef.driver.repo

import com.google.gson.annotations.SerializedName

data class ModelData(
    @SerializedName("Model_Name")
    val name: String
)
