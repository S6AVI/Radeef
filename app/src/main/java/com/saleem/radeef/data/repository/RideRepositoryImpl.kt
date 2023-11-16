package com.saleem.radeef.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.saleem.radeef.data.firestore.Ride
import com.saleem.radeef.data.firestore.RideStatus
import com.saleem.radeef.util.FirestoreTables
import com.saleem.radeef.util.UiState
import com.saleem.radeef.util.logD

class RideRepositoryImpl(
    val database: FirebaseFirestore,
    val auth: FirebaseAuth
) : RideRepository {

    override fun getPassengerRides(result: (UiState<List<Ride>>) -> Unit) {
        database.collection(FirestoreTables.RIDES)
            .whereEqualTo("passengerID", auth.currentUser?.uid)
            .get()
            .addOnSuccessListener {
                Log.d("savii", "success in getRides()")
                val rides = arrayListOf<Ride>()
                for (document in it) {
                    val ride = document.toObject(Ride::class.java)
                    ride.rideID = document.id
                    rides.add(ride)
                }
                result.invoke(
                    UiState.Success(rides)
                )
            }
            .addOnFailureListener {
                Log.d("savii", "failure in getRides()")
                result.invoke(
                    UiState.Failure(
                        it.localizedMessage
                    )
                )
            }
//        val data = arrayListOf(
//            Ride(
//                pickupLocation = LatLng.getDefaultInstance(),
//                destination = LatLng.getDefaultInstance(),
//                chargeAmount = 15.0,
//                startTime = DateTime.getDefaultInstance(),
//                endTime = DateTime.getDefaultInstance(),
//                passengerID = 0
//            )
//        )

//        if (data.isNullOrEmpty()) {
//            return UiState.Failure("data is empty")
//        } else {
//            return UiState.Success(data)
//        }
    }

    override fun getAllRidesRequests(result: (UiState<List<Ride>>) -> Unit) {
        database.collection(FirestoreTables.RIDES)
            .whereEqualTo("status", RideStatus.SEARCHING_FOR_DRIVER.value)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    result.invoke(UiState.Failure(error.localizedMessage))
                    return@addSnapshotListener
                }

                val rides = arrayListOf<Ride>()
                for (document in snapshot?.documents ?: emptyList()) {
                    logD("document data: ${document.data}")
                    logD("passengerName: ")
                    logD(document.getString("passengerName").toString())
                    val id = document.getString("driverId")
                    logD("driverId: ${id.toString()}")
                    val ride = document.toObject(Ride::class.java)
                    ride?.rideID = document.id
                    logD("ride fetched: $ride")
                    ride?.let { rides.add(it) }
                }
                result.invoke(UiState.Success(rides))
            }
    }

    override fun addRide(ride: Ride, result: (UiState<String>) -> Unit) {
        val document = database.collection(FirestoreTables.RIDES).document()
        ride.rideID = document.id
        document
            .set(ride)
            .addOnSuccessListener {
                result.invoke(
                    UiState.Success("Ride has been created")
                )
            }
            .addOnFailureListener {
                result.invoke(
                    UiState.Failure(
                        it.localizedMessage
                    )
                )
            }
    }
}

