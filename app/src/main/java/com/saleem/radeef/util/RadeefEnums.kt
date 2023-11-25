package com.saleem.radeef.util


enum class Sex(val value: String) {
    MALE("MALE"),
    FEMALE("FEMALE"),
    NOTSPECIFIED("none")
}

enum class PassengerStatus(val value: String) {
    INACTIVE("INACTIVE"),
    SEARCHING("SEARCHING"),
    IN_RIDE("IN_RIDE"),
}