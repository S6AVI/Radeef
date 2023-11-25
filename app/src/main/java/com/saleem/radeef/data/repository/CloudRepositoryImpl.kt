package com.saleem.radeef.data.repository

import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.saleem.radeef.data.firestore.Passenger
import com.saleem.radeef.passenger.ui.map.TAG
import com.saleem.radeef.util.FirestoreTables
import com.saleem.radeef.util.UiState
import com.saleem.radeef.util.toGeoPoint

class CloudRepositoryImpl(
    val database: FirebaseFirestore,
    val auth: FirebaseAuth
) : CloudRepository {

    override fun updatePassengerInfo(passenger: Passenger, result: (UiState<String>) -> Unit) {
        val passengersCollection = database.collection("passengers")
        //Log.d(TAG, passengersCollection.id.toString())
        Log.d(TAG, "name:${passenger.name}\nemail:${passenger.email}\ngender:${passenger.sex}")
        val data = mapOf(
            "name" to passenger.name,
            "email" to passenger.email,
            "gender" to passenger.sex
        )

        Log.d(TAG, data.toString())
        passengersCollection.document(auth.currentUser?.uid.toString())
            .update(data.toMap())
            .addOnSuccessListener {
                result.invoke(UiState.Success("data is successfully updated"))
            }
            .addOnFailureListener {
                result.invoke(UiState.Failure(it.message.toString()))

            }
    }

    override fun getPassengerName(id: String, result: (UiState<String>) -> Unit) {
        val passengersCollection = database.collection(FirestoreTables.PASSENGERS)
        passengersCollection.document(id).get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val passengerName = documentSnapshot.getString("name")
                    result(UiState.Success(passengerName ?: ""))

                } else {
                    result(UiState.Failure("Passenger not found"))
                }
            }
            .addOnFailureListener { exception ->
                result(UiState.Failure(exception.message ?: "Failed to retrieve passenger name"))
            }
    }

    override fun getPassenger(id: String, result: (UiState<Passenger?>) -> Unit) {
        val passengersCollection = database.collection(FirestoreTables.PASSENGERS)
            .document(id)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                val passenger = documentSnapshot.toObject(Passenger::class.java)
                result.invoke(UiState.Success(passenger))
            }
            .addOnFailureListener { exception ->
                result.invoke(UiState.Failure(exception.localizedMessage))
            }
    }

    override fun getPassenger(result: (UiState<Passenger>) -> Unit) {
        val passengerId = auth.currentUser?.uid

        if (passengerId != null) {
            val documentRef = database.collection(FirestoreTables.PASSENGERS)
                .document(passengerId)

            val registration = documentRef.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // Handle error
                    result.invoke(UiState.Failure(error.message.toString()))
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val passenger = snapshot.toObject(Passenger::class.java)
                    if (passenger != null) {
                        result.invoke(UiState.Success(passenger))
                    } else {
                        // Handle error: unable to parse driver data
                        result.invoke(UiState.Failure("Failed to parse passenger data"))
                    }
                } else {
                    // Handle error: driver document not found
                    result.invoke(UiState.Failure("Passenger document not found"))
                }
            }
        } else {

            result.invoke(UiState.Failure("Passenger ID is null"))
        }
    }

    override fun updatePassengerLocations(pickup: LatLng, destination: LatLng, result: (UiState<Boolean>) -> Unit) {
        val data = mapOf(
            "pickup" to pickup.toGeoPoint(),
            "destination" to destination.toGeoPoint()
        )

        database.collection(FirestoreTables.PASSENGERS).document(auth.currentUser!!.uid)
            .update(data)
            .addOnSuccessListener {
                result(UiState.Success(true))
            }.addOnFailureListener {
                result(UiState.Failure(it.message))
            }
    }
}