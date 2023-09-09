package com.saleem.radeef.driver.repo

import com.google.gson.annotations.SerializedName

data class MakeData(
    @SerializedName("MakeName")
    val name: String
)
