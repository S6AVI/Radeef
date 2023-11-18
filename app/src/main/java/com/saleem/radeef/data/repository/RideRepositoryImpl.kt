package com.saleem.radeef.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.saleem.radeef.data.firestore.HiddenRides
import com.saleem.radeef.data.firestore.Ride
import com.saleem.radeef.data.firestore.RideStatus
import com.saleem.radeef.data.firestore.driver.Driver
import com.saleem.radeef.driver.ui.home.RideWithDistance
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
        val driverId = auth.currentUser!!.uid
        val ridesCollection = database.collection(FirestoreTables.RIDES)
        val hiddenRidesCollection = database.collection(FirestoreTables.HIDDEN_RIDES)

        val allRidesQuery =
            ridesCollection.whereEqualTo("status", RideStatus.SEARCHING_FOR_DRIVER.value)

        allRidesQuery.addSnapshotListener { allRidesSnapshot, error ->
            if (error != null) {
                result.invoke(UiState.Failure(error.localizedMessage))
                return@addSnapshotListener
            }

            val allRides = allRidesSnapshot?.documents?.mapNotNull { document ->
                val ride = document.toObject(Ride::class.java)
                ride?.rideID = document.id
                ride
            } ?: emptyList()

            hiddenRidesCollection
                .whereEqualTo("driverId", driverId)
                .addSnapshotListener { hiddenRidesQuerySnapshot, hiddenRidesError ->
                    if (hiddenRidesError != null) {
                        result.invoke(UiState.Failure(hiddenRidesError.localizedMessage))
                        return@addSnapshotListener
                    }

                    val hiddenRideIds =
                        hiddenRidesQuerySnapshot?.documents?.map { it.getString("rideId") }
                            ?: emptyList()

                    val filteredRides = allRides.filter { ride ->
                        ride.rideID !in hiddenRideIds
                    }

                    result.invoke(UiState.Success(filteredRides))
                }
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

    override fun hideRide(rideId: String, result: (UiState<String>) -> Unit) {
        val driverId = auth.currentUser!!.uid
        val hiddenRide = HiddenRides(rideId = rideId, driverId = driverId)
        val hiddenRidesCollection = database.collection(FirestoreTables.HIDDEN_RIDES)

        val query = hiddenRidesCollection
            .whereEqualTo("driverId", driverId)
            .whereEqualTo("rideId", rideId)

        query.get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    // No existing document found, add a new one
                    val newDocumentReference = hiddenRidesCollection.document()
                    newDocumentReference.set(hiddenRide)
                        .addOnSuccessListener {
                            // Document set successfully
                            result(UiState.Success(newDocumentReference.id))
                        }
                        .addOnFailureListener { exception ->
                            // Error occurred while setting the document
                            result(UiState.Failure(exception.message.toString()))
                        }
                } else {
                    // Document with the same driverId and rideId already exists
                    result(UiState.Success("Document already exists"))
                }
            }
            .addOnFailureListener { exception ->
                // Error occurred while querying the documents
                result(UiState.Failure(exception.message.toString()))
            }
    }

    override fun updateRideState(
        rideWithDistance: RideWithDistance,
        driver: Driver,
        status: String,
        result: (UiState<String>) -> Unit
    ) {
        val rideRef =
            database.collection(FirestoreTables.RIDES).document(rideWithDistance.ride.rideID)

        logD("inside updateRideState - cost: ${rideWithDistance.ride.chargeAmount}")
        val updates = hashMapOf<String, Any>(
            "status" to status,
            "driverId" to driver.driverID,
            "driverName" to driver.name,
            "driverLocation" to driver.pickup,
            "chargeAmount" to rideWithDistance.ride.chargeAmount,
            "distance" to rideWithDistance.distance
        )
        rideRef.update(updates)
            .addOnSuccessListener {
                result.invoke(UiState.Success("Ride state updated successfully"))
            }
            .addOnFailureListener { exception ->
                result.invoke(UiState.Failure(exception.localizedMessage))
            }
    }

    override fun getCurrentRide(result: (UiState<Ride?>) -> Unit) {
        database.collection(FirestoreTables.RIDES)
            .whereEqualTo("driverId", auth.currentUser!!.uid)
            .whereEqualTo("status", RideStatus.WAITING_FOR_CONFIRMATION)
            .limit(1)
            .addSnapshotListener { value, error ->
                if (error != null) {
                    result.invoke(UiState.Failure(error.localizedMessage))
                    return@addSnapshotListener
                }

                val ride = value?.documents?.firstOrNull()?.toObject(Ride::class.java)
                result.invoke(UiState.Success(ride))
            }
    }
}

