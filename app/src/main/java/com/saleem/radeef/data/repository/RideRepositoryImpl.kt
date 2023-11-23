package com.saleem.radeef.data.repository

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Source
import com.saleem.radeef.data.firestore.HiddenRides
import com.saleem.radeef.data.firestore.Ride
import com.saleem.radeef.data.firestore.RideStatus
import com.saleem.radeef.data.firestore.driver.Driver
import com.saleem.radeef.data.firestore.driver.UserStatus
import com.saleem.radeef.driver.DriverHomeUiState
import com.saleem.radeef.driver.ui.home.RideWithDistance
import com.saleem.radeef.util.FirestoreTables
import com.saleem.radeef.util.UiState
import com.saleem.radeef.util.logD
import java.util.Date

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

    override fun getDriverRides(result: (UiState<List<Ride>>) -> Unit) {
        database.collection(FirestoreTables.RIDES)
            .whereEqualTo("driverId", auth.currentUser?.uid)
            .whereIn("status", listOf("ARRIVED", "CANCELED")) // Add this line to filter by status
            .get()
            .addOnSuccessListener { querySnapshot ->
                Log.d("savii", "success in getRides()")
                val rides = arrayListOf<Ride>()
                for (document in querySnapshot) {
                    val ride = document.toObject(Ride::class.java)
                    ride.rideID = document.id
                    rides.add(ride)
                }
                result.invoke(UiState.Success(rides))
            }
            .addOnFailureListener { exception ->
                Log.d("savii", "failure in getRides()")
                result.invoke(UiState.Failure(exception.localizedMessage))
            }
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
                logD("document: ${document.data}")
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

//    override fun getCurrentRide(result: (UiState<Ride?>) -> Unit) {
////        val defaultTime = Timestamp(Date())
////        logD("default date: $defaultTime")
//        database.collection(FirestoreTables.RIDES)
//            .whereEqualTo("driverId", auth.currentUser!!.uid)
//            //.whereEqualTo("status", RideStatus.WAITING_FOR_CONFIRMATION)
//            //.orderBy("startTime", Query.Direction.DESCENDING)
//            .limit(1)
//            .addSnapshotListener(MetadataChanges.INCLUDE) { value, error ->
//                if (error != null) {
//                    result.invoke(UiState.Failure(error.localizedMessage))
//                    return@addSnapshotListener
//                }
//                val ride = value?.documents?.firstOrNull()?.toObject(Ride::class.java)
//                logD("get current ride: $ride")
//                ride?.rideID = value?.documents?.firstOrNull()!!.id
//                result.invoke(UiState.Success(ride))
//            }
//    }

    override fun getCurrentRide(result: (UiState<Ride?>) -> Unit) {
        database.collection(FirestoreTables.RIDES)
            .whereEqualTo("driverId", auth.currentUser!!.uid)
            .addSnapshotListener { querySnapshot, error ->
                if (error != null) {
                    result.invoke(UiState.Failure(error.localizedMessage))
                    return@addSnapshotListener
                }

                val rides = querySnapshot?.documents?.mapNotNull { document ->
                    document.toObject(Ride::class.java)?.apply {
                        rideID = document.id
                    }
                }

                val currentRide: Ride? = rides?.maxByOrNull { it.startTime }

                result.invoke(UiState.Success(currentRide))
            }
    }

    override fun cancelWaitingRide(ride: Ride, result: (UiState<String>) -> Unit) {
        logD("rideId: ${ride.rideID}")
        val document = database.collection(FirestoreTables.RIDES)
            .document(ride.rideID)

        val hiddenRidesCollection = database.collection(FirestoreTables.HIDDEN_RIDES)

        val updates = hashMapOf<String, Any>(
            "status" to RideStatus.SEARCHING_FOR_DRIVER.value,
            "driverId" to "",
            "driverName" to "",
            "driverLocation" to GeoPoint(.0, .0),
            "chargeAmount" to 0.0,
            "distance" to 0.0
        )
        database.runBatch { batch ->
            batch.update(document, updates)

            val hiddenRideDocument = hiddenRidesCollection.document()
            logD("driver: ${ride.driverId}\nride: ${ride.rideID}")
            val hiddenRideData = hashMapOf(
                "driverId" to ride.driverId,
                "rideId" to ride.rideID
            )
            batch.set(hiddenRideDocument, hiddenRideData)
        }
            .addOnSuccessListener {
                result.invoke(UiState.Success("Ride state updated successfully"))
            }
            .addOnFailureListener { exception ->
                result.invoke(UiState.Failure(exception.localizedMessage))
            }
    }

    override fun cancelRide(ride: Ride, result: (UiState<String>) -> Unit) {
        val rideDocument = database.collection(FirestoreTables.RIDES)
            .document(ride.rideID)

        val driverDocument = database.collection(FirestoreTables.DRIVERS).document(ride.driverId)
        val updates = hashMapOf<String, Any>(
            "status" to RideStatus.CANCELED.value,
        )

        val updateDriver = hashMapOf<String, Any>(
            "status" to UserStatus.SEARCHING,
        )
        database.runBatch { batch ->
            batch.update(rideDocument, updates)
            batch.update(driverDocument, updateDriver)
        }
            .addOnSuccessListener {
                result.invoke(UiState.Success("Ride cancelled successfully"))
            }
            .addOnFailureListener { exception ->
                result.invoke(UiState.Failure(exception.localizedMessage))
            }
    }

    override fun updateCurrentRideState(ride: Ride, status: String, result: (UiState<String>) -> Unit) {
        logD("update current ride status: ${ride.status}, $status")
        val rideDocument = database.collection(FirestoreTables.RIDES)
            .document(ride.rideID)

        val updates = hashMapOf<String, Any>(
            "status" to status,
        )
        rideDocument
            .update(updates)
            .addOnSuccessListener {
                result.invoke(UiState.Success("Ride updated successfully: ${ride.status}"))
            }
            .addOnFailureListener { exception ->
                result.invoke(UiState.Failure(exception.localizedMessage))
            }

    }
}

