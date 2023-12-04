package com.saleem.radeef.util


enum class Gender(val value: String) {
    MALE("MALE"),
    FEMALE("FEMALE"),
    NONE("none")
}

enum class PassengerStatus(val value: String) {
    INACTIVE("INACTIVE"),
    SEARCHING("SEARCHING"),
    IN_RIDE("IN_RIDE"),
}

enum class DriverStatus(val value: String) {
    INACTIVE("INACTIVE"),
    SEARCHING("SEARCHING"),
    IN_RIDE("IN_RIDE"),
    CONTINUE("CONTINUE")
}

enum class RegistrationStatus(val value: String) {
    INFO("INFO"),
    LICENSE("LICENSE"),
    VEHICLE("VEHICLE"),
    COMPLETED("COMPLETED")
}

enum class BloodType(val value: String) {
    A_POSITIVE("A+"),
    A_NEGATIVE("A-"),
    B_POSITIVE("B+"),
    B_NEGATIVE("B-"),
    AB_POSITIVE("AB+"),
    AB_NEGATIVE("AB-"),
    O_POSITIVE("O+"),
    O_NEGATIVE("O-")
}

enum class RideStatus(val value: String) {
    SEARCHING_FOR_DRIVER("SEARCHING_FOR_DRIVER"),
    WAITING_FOR_CONFIRMATION("WAITING_FOR_CONFIRMATION"),
    PASSENGER_PICK_UP("PASSENGER_PICK_UP"),
    EN_ROUTE("EN_ROUTE"),
    ARRIVED("ARRIVED"),
    CANCELED("CANCELED"),
}