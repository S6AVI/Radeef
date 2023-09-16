package com.saleem.radeef.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.saleem.radeef.data.firestore.Driver
import com.saleem.radeef.ui.map.TAG
import com.saleem.radeef.util.UiState

class CloudRepositoryImpl(
    val database: FirebaseFirestore,
    val auth: FirebaseAuth
) : CloudRepository {

    override fun updatePassengerInfo(passenger: Driver, result: (UiState<String>) -> Unit) {
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
}