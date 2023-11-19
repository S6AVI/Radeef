package com.saleem.radeef.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.saleem.radeef.data.firestore.Passenger
import com.saleem.radeef.passenger.ui.map.TAG
import com.saleem.radeef.util.FirestoreTables
import com.saleem.radeef.util.UiState

class CloudRepositoryImpl(
    val database: FirebaseFirestore,
    val auth: FirebaseAuth
) : CloudRepository {

    override fun updatePassengerInfo(passenger: Passenger, result: (UiState<String>) -> Unit) {
        val passengersCollection = database.collection("passengers")
        //Log.d(TAG, passengersCollection.id.toString())
        Log.d(TAG, "name:${passenger.name}\nemail:${passenger.email}\ngender:${passenger.gender}")
        val data = mapOf(
            "name" to passenger.name,
            "email" to passenger.email,
            "gender" to passenger.gender
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
}