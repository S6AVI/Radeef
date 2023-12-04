package com.saleem.radeef.data.model

import com.google.gson.annotations.SerializedName

/*
data class to convert Json attribute into Kotlin field
 */
data class ModelData(
    @SerializedName("Model_Name")
    val name: String
)
