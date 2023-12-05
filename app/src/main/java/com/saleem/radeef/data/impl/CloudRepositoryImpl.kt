package com.saleem.radeef.data.impl

import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.saleem.radeef.data.model.Passenger
import com.saleem.radeef.data.repository.CloudRepository
import com.saleem.radeef.util.FirestoreTables
import com.saleem.radeef.util.UiState
import com.saleem.radeef.util.toGeoPoint

class CloudRepositoryImpl(
    val database: FirebaseFirestore,
    val auth: FirebaseAuth
) : CloudRepository {


    // update passenger info; for profile
    override fun updatePassengerInfo(passenger: Passenger, result: (UiState<String>) -> Unit) {
        val passengersCollection = database.collection(FirestoreTables.PASSENGERS)

        val data = mapOf(
            "name" to passenger.name,
            "email" to passenger.email,
            "gender" to passenger.gender
        )

        passengersCollection.document(auth.currentUser?.uid.toString())
            .update(data.toMap())
            .addOnSuccessListener {
                result.invoke(UiState.Success("data is successfully updated"))
            }
            .addOnFailureListener {
                result.invoke(UiState.Failure(it.message.toString()))

            }
    }


    // get passenger; for driver
    override fun getPassenger(id: String, result: (UiState<Passenger?>) -> Unit) {
        database.collection(FirestoreTables.PASSENGERS)
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

    // get passenger; and listen to changes
    override fun getPassenger(result: (UiState<Passenger>) -> Unit) {
        val passengerId = auth.currentUser?.uid

        if (passengerId != null) {
            val documentRef = database.collection(FirestoreTables.PASSENGERS)
                .document(passengerId)

            documentRef.addSnapshotListener { snapshot, error ->
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
                        result.invoke(UiState.Failure("Failed to parse passenger data"))
                    }
                } else {
                    result.invoke(UiState.Failure("Passenger document not found"))
                }
            }
        } else {
            result.invoke(UiState.Failure("Passenger ID is null"))
        }
    }

    // store passenger locations
    override fun updatePassengerLocations(pickup: LatLng, destination: LatLng, result: (UiState<Boolean>) -> Unit) {

        val passengerId = auth.currentUser!!.uid

        val data = mapOf(
            "pickup" to pickup.toGeoPoint(),
            "destination" to destination.toGeoPoint()
        )

        database.collection(FirestoreTables.PASSENGERS).document(passengerId)
            .update(data)
            .addOnSuccessListener {
                result(UiState.Success(true))
            }.addOnFailureListener {
                result(UiState.Failure(it.message))
            }
    }

    // update status of passenger
    override fun updatePassengerStatus(status: String, result: (UiState<String>) -> Unit) {

        val passengerId = auth.currentUser?.uid!!
        val passengerDocumentRef =
            database.collection(FirestoreTables.PASSENGERS).document(passengerId)

        passengerDocumentRef.update("status", status)
            .addOnSuccessListener {
                result(UiState.Success(status))
            }
            .addOnFailureListener { exception ->
                result(UiState.Failure("Failed to update passenger state: ${exception.localizedMessage}"))
            }
    }
}