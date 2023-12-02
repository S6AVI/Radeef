package com.saleem.radeef.driver.repo

import android.app.Activity
import android.net.Uri
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.storage.StorageReference
import com.saleem.radeef.data.RadeefLocation
import com.saleem.radeef.data.firestore.Passenger

import com.saleem.radeef.data.firestore.driver.Driver
import com.saleem.radeef.data.firestore.driver.DriverWithVehicle
import com.saleem.radeef.data.firestore.driver.License
import com.saleem.radeef.data.firestore.driver.RegistrationStatus
import com.saleem.radeef.data.firestore.driver.UserStatus
import com.saleem.radeef.data.firestore.driver.Vehicle
import com.saleem.radeef.passenger.ui.map.TAG
import com.saleem.radeef.util.FirebaseStorageConstants.DRIVER_DIRECTORY
import com.saleem.radeef.util.FirestoreTables
import com.saleem.radeef.util.UiState
import com.saleem.radeef.util.logD
import com.saleem.radeef.util.toGeoPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.lang.Exception
import java.util.concurrent.TimeUnit

class DriverRepositoryImpl(
    private val database: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val storageReference: StorageReference

) : DriverRepository {


    private lateinit var verificationId: String
    private lateinit var driver: Driver
    private lateinit var token: PhoneAuthProvider.ForceResendingToken


    override fun registerDriver(
        driver: Driver,
        phone: String,
        activity: Activity,
        result: (UiState<String>) -> Unit
    ) {


        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phone)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            //.setCallbacks(callbacks) // OnVerificationStateChangedCallbacks
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    Log.d(TAG, "registerDriver: onverifcomp: line 41")
                    auth.signInWithCredential(credential)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                driver.driverID = task.result.user?.uid ?: ""
                                updateDriverInfo(driver) { state ->
                                    when (state) {
                                        is UiState.Success -> {
                                        }

                                        is UiState.Failure -> {
                                            result.invoke(UiState.Failure(state.error))
                                        }

                                        else -> {}
                                    }
                                }
                            } else {
                                logD("Authentication failed, ${task.exception?.message ?: "unknown error"}")
                                result.invoke(
                                    UiState.Failure(
                                        "Authentication failed, ${task.exception?.message ?: "unknown error"}"
                                    )
                                )
                            }
                        }
                        .addOnFailureListener {
                            result.invoke(
                                UiState.Failure(
                                    it.localizedMessage
                                )
                            )
                        }
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    logD("Verification failed, ${e.message ?: "unknown error"}")
                    result.invoke(UiState.Failure("Verification failed, ${e.message ?: "unknown error"}"))
                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    this@DriverRepositoryImpl.driver = driver
                    this@DriverRepositoryImpl.verificationId = verificationId
                    this@DriverRepositoryImpl.token = token
                    result.invoke(UiState.Success("Verification code sent"))
                }
            })
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }


    override fun updateDriverInfo(driver: Driver, result: (UiState<Driver>) -> Unit) {
        val documentCollection = database.collection(FirestoreTables.DRIVERS)
        documentCollection
            .whereEqualTo("phoneNumber", driver.phoneNumber)
            .get()
            .addOnSuccessListener {
                if (it.documents.isNotEmpty()) {
                    val retrievedDriver = it.documents[0].toObject(Driver::class.java)
                    if (retrievedDriver != null) {
                        result.invoke(
                            UiState.Success(retrievedDriver)
                        )
                    }
                } else {
                    documentCollection.document(driver.driverID)
                        .set(driver)
                        .addOnSuccessListener {
                            result.invoke(
                                UiState.Success(driver)
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
            .addOnFailureListener {
                result.invoke(
                    UiState.Failure(
                        it.localizedMessage
                    )
                )
            }
    }

    override fun isPhoneNumberAssociatedWithPassenger(phone: String, callback: (Boolean) -> Unit) {
        val passengersCollection = database.collection(FirestoreTables.PASSENGERS)

        val query = passengersCollection.whereEqualTo("phoneNumber", phone)

        query.get().addOnSuccessListener { querySnapshot ->
            val passengers = querySnapshot.toObjects(Passenger::class.java)
            if (passengers.isNotEmpty()) {
                // Phone number is associated with a passenger
                callback(true)
            } else {
                // Phone number is not associated with any passenger
                callback(false)
            }
        }.addOnFailureListener { exception ->
            // Handle any errors that occurred while retrieving data
            callback(false)
        }
    }

    override fun logout(result: (UiState<String>) -> Unit) {
        auth.signOut()
        result.invoke(UiState.Success("user signed out"))
    }

    override fun signIn(code: String, result: (UiState<Driver>) -> Unit) {
        val credential = PhoneAuthProvider.getCredential(verificationId, code)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    driver.driverID = task.result?.user?.uid ?: ""
                    updateDriverInfo(driver) { state ->
                        when (state) {
                            is UiState.Success -> {
                                result.invoke(UiState.Success(state.data))
                            }

                            is UiState.Failure -> {
                                result.invoke(UiState.Failure(state.error))
                            }

                            else -> {}
                        }
                    }
                } else {
                    result.invoke(
                        UiState.Failure(
                            "Authentication failed, ${task.exception?.message ?: "unknown error"}"
                        )
                    )
                }
            }
            .addOnFailureListener {
                result.invoke(
                    UiState.Failure(
                        it.localizedMessage
                    )
                )
            }
    }

    override fun updateName(name: String, result: (UiState<String>) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun isRegistered(): Boolean {
        return auth.currentUser != null
    }

    override fun resendCode(activity: Activity, result: (UiState<String>) -> Unit) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(driver.phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    // This method will be called if the user has already been verified
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    result.invoke(UiState.Failure("Verification failed, ${e.message ?: "unknown error"}"))
                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    this@DriverRepositoryImpl.verificationId = verificationId
                    result.invoke(UiState.Success("Verification code sent"))
                }
            })
            .setForceResendingToken(token)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    override fun hasName(callback: (Boolean) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun getName(result: (UiState<String>) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun getDriver(result: (UiState<Driver>) -> Unit) {
        val driverId = auth.currentUser?.uid

        if (driverId != null) {
            val driverDocumentRef = database.collection(FirestoreTables.DRIVERS)
                .document(driverId)

            val registration = driverDocumentRef.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // Handle error
                    result.invoke(UiState.Failure(error.message.toString()))
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val driver = snapshot.toObject(Driver::class.java)
                    if (driver != null) {
                        result.invoke(UiState.Success(driver))
                    } else {
                        // Handle error: unable to parse driver data
                        result.invoke(UiState.Failure("Failed to parse driver data"))
                    }
                } else {
                    // Handle error: driver document not found
                    result.invoke(UiState.Failure("Driver document not found"))
                }
            }

        } else {
            result.invoke(UiState.Failure("Driver ID is null"))
        }
    }

    override fun updateDriverCurrentLocation(pickup: LatLng, result: (UiState<Boolean>) -> Unit) {
        val data = mapOf(
            "pickup" to pickup.toGeoPoint()
        )

        database.collection(FirestoreTables.DRIVERS).document(auth.currentUser!!.uid)
            .update(data)
            .addOnSuccessListener {
                result(UiState.Success(true))
            }.addOnFailureListener {
                result(UiState.Failure(it.message))
            }
    }

    override fun updateDriverDestination(destination: LatLng, result: (UiState<Boolean>) -> Unit) {
        val data = mapOf(
            "destination" to destination.toGeoPoint()
        )

        database.collection(FirestoreTables.DRIVERS).document(auth.currentUser!!.uid)
            .update(data)
            .addOnSuccessListener {
                result(UiState.Success(true))
            }.addOnFailureListener {
                result(UiState.Failure(it.message))
            }
    }

    override fun updateDriver(driver: Driver, result: (UiState<String>) -> Unit) {
        val driverWithId = driver.copy(
            driverID = auth.currentUser!!.uid,
            phoneNumber = auth.currentUser!!.phoneNumber!!
        )
        val driverDocument =
            database.collection(FirestoreTables.DRIVERS).document(driverWithId.driverID)
                .set(driverWithId)
                .addOnSuccessListener {
                    result.invoke(UiState.Success("Driver updated successfully"))
                }
                .addOnFailureListener { exception ->
                    result.invoke(UiState.Failure(exception.message))
                }
    }

    override fun getDriver(id: String, result: (UiState<DriverWithVehicle?>) -> Unit) {
        val driverDocumentRef = database.collection(FirestoreTables.DRIVERS).document(id)

        driverDocumentRef.addSnapshotListener { driverSnapshot, exception ->
            if (exception != null) {
                result.invoke(UiState.Failure("Failed to fetch driver data: ${exception.localizedMessage}"))
                return@addSnapshotListener
            }

            if (driverSnapshot != null && driverSnapshot.exists()) {
                val driver = driverSnapshot.toObject(Driver::class.java)

                if (driver != null) {
                    val vehicleQuery = database.collection(FirestoreTables.VEHICLES)
                        .whereEqualTo("driverID", driver.driverID)
                        .limit(1)

                    vehicleQuery.get()
                        .addOnSuccessListener { vehicleQuerySnapshot ->
                            if (!vehicleQuerySnapshot.isEmpty) {
                                val vehicleSnapshot = vehicleQuerySnapshot.documents[0]
                                val vehicle = vehicleSnapshot.toObject(Vehicle::class.java)

                                val driverWithVehicle = DriverWithVehicle(driver, vehicle)
                                result.invoke(UiState.Success(driverWithVehicle))
                            } else {
                                val driverWithVehicle = DriverWithVehicle(driver, null)
                                result.invoke(UiState.Success(driverWithVehicle))
                            }
                        }
                        .addOnFailureListener { exception ->
                            result.invoke(UiState.Failure("Failed to fetch vehicle data: ${exception.localizedMessage}"))
                        }
                } else {
                    result.invoke(UiState.Failure("Driver is null"))
                }
            } else {
                result.invoke(UiState.Failure("Driver document not found"))
            }
        }
    }

    override fun getDriverWhenArrived(id: String, result: (UiState<DriverWithVehicle?>) -> Unit) {
        val driverDocumentRef = database.collection(FirestoreTables.DRIVERS).document(id)

        driverDocumentRef.get()
            .addOnSuccessListener { driverSnapshot ->
                if (driverSnapshot.exists()) {
                    val driver = driverSnapshot.toObject(Driver::class.java)

                    if (driver != null) {
                        val vehicleQuery = database.collection(FirestoreTables.VEHICLES)
                            .whereEqualTo("driverID", driver.driverID)
                            .limit(1)

                        vehicleQuery.get()
                            .addOnSuccessListener { vehicleQuerySnapshot ->
                                if (!vehicleQuerySnapshot.isEmpty) {
                                    val vehicleSnapshot = vehicleQuerySnapshot.documents[0]
                                    val vehicle = vehicleSnapshot.toObject(Vehicle::class.java)

                                    val driverWithVehicle = DriverWithVehicle(driver, vehicle)
                                    result.invoke(UiState.Success(driverWithVehicle))
                                } else {
                                    val driverWithVehicle = DriverWithVehicle(driver, null)
                                    result.invoke(UiState.Success(driverWithVehicle))
                                }
                            }
                            .addOnFailureListener { exception ->
                                result.invoke(UiState.Failure("Failed to fetch vehicle data: ${exception.localizedMessage}"))
                            }
                    } else {
                        result.invoke(UiState.Failure("Driver is null"))
                    }
                } else {
                    result.invoke(UiState.Failure("Driver document not found"))
                }
            }
            .addOnFailureListener { exception ->
                result.invoke(UiState.Failure("Failed to fetch driver data: ${exception.localizedMessage}"))
            }
    }


    override fun getVehicle(result: (UiState<Vehicle>) -> Unit) {
        database.collection(FirestoreTables.VEHICLES)
            .whereEqualTo("driverID", auth.currentUser?.uid)
            .limit(1)
            .get()
            .addOnSuccessListener {
                if (!it.isEmpty) {
                    val documentSnapshot = it.documents[0]
                    val vehicle = documentSnapshot.toObject(Vehicle::class.java)!!

                    result.invoke(
                        UiState.Success(vehicle.copy(vehicleID = documentSnapshot.id))
                    )
                } else {
                    createEmptyVehicle(result)
                }

            }
            .addOnFailureListener { exception ->
                result.invoke(
                    UiState.Failure(
                        exception.message.toString()
                    )
                )
            }
    }

    override fun getLicense(result: (UiState<License>) -> Unit) {
        database.collection(FirestoreTables.LICENSE)
            .whereEqualTo("driverID", auth.currentUser?.uid)
            .limit(1)
            .get()
            .addOnSuccessListener {
                if (!it.isEmpty) {
                    val documentSnapshot = it.documents[0]
                    val license = documentSnapshot.toObject(License::class.java)!!

                    result.invoke(
                        UiState.Success(license.copy(licenseID = documentSnapshot.id))
                    )
                } else {
                    createEmptyLicense(result)
                }

            }
            .addOnFailureListener { exception ->
                result.invoke(
                    UiState.Failure(
                        exception.message.toString()
                    )
                )
            }
    }

    override fun createEmptyLicense(result: (UiState<License>) -> Unit) {
        val newLicense = License(driverID = auth.currentUser?.uid!!)

        database.collection(FirestoreTables.LICENSE)
            .add(newLicense)
            .addOnSuccessListener { documentReference ->
                val licenseWithId = newLicense.copy(licenseID = documentReference.id)
                result.invoke(UiState.Success(licenseWithId))
            }
            .addOnFailureListener { exception ->
                result.invoke(UiState.Failure(exception.message.toString()))
            }
    }

    private fun createEmptyVehicle(result: (UiState<Vehicle>) -> Unit) {
        val newVehicle = Vehicle(driverID = auth.currentUser?.uid!!)

        database.collection(FirestoreTables.VEHICLES)
            .add(newVehicle)
            .addOnSuccessListener { documentReference ->
                val vehicleWithId = newVehicle.copy(vehicleID = documentReference.id)
                result.invoke(UiState.Success(vehicleWithId))
            }
            .addOnFailureListener { exception ->
                result.invoke(UiState.Failure(exception.message.toString()))
            }
    }

    override fun updateLicense(license: License, result: (UiState<String>) -> Unit) {
        val driverId = auth.currentUser!!.uid
        val batch = database.batch()
        val driverRef = database.collection(FirestoreTables.DRIVERS).document(driverId)

        val data = hashMapOf<String, Any>(
            "registrationStatus" to RegistrationStatus.VEHICLE.value
        )

        batch.update(driverRef, data)

        val licenseWithUid = license.copy(driverID = driverId)
        database.collection(FirestoreTables.LICENSE).document(license.licenseID)
            .set(licenseWithUid)
            .addOnSuccessListener {
                batch.commit()
                    .addOnSuccessListener {
                        result.invoke(UiState.Success("License updated successfully"))
                    }
                    .addOnFailureListener { exception ->
                        result.invoke(UiState.Failure(exception.message))
                    }
            }
            .addOnFailureListener { exception ->
                result.invoke(UiState.Failure(exception.message))
            }
    }

    override fun updateVehicle(vehicle: Vehicle, result: (UiState<String>) -> Unit) {
        val driverId = auth.currentUser!!.uid
        val batch = database.batch()
        val driverRef = database.collection(FirestoreTables.DRIVERS).document(driverId)

        val driverData = hashMapOf<String, Any>(
            "registrationStatus" to RegistrationStatus.COMPLETED.value
        )

        batch.update(driverRef, driverData)

        val vehicleWithUid = vehicle.copy(driverID = driverId)
        val vehicleRef = database.collection(FirestoreTables.VEHICLES).document(vehicle.vehicleID)
        batch.set(vehicleRef, vehicleWithUid)

        batch.commit()
            .addOnSuccessListener {
                result.invoke(UiState.Success("Vehicle updated successfully"))
            }
            .addOnFailureListener { exception ->
                result.invoke(UiState.Failure(exception.message))
            }
    }


    override suspend fun uploadLicenseFile(fileUrl: Uri, onResult: (UiState<Uri>) -> Unit) {
        try {
            val uri: Uri = withContext(Dispatchers.IO) {
                storageReference
                    .putFile(fileUrl)
                    .await()
                    .storage
                    .downloadUrl
                    .await()
            }
            onResult.invoke(UiState.Success(uri))

        } catch (e: FirebaseFirestoreException) {
            onResult.invoke(UiState.Failure(e.message))
        } catch (e: Exception) {
            onResult.invoke(UiState.Failure(e.message))
        }
    }

    override suspend fun uploadImage(fileUrl: Uri, name: String, onResult: (UiState<Uri>) -> Unit) {
        try {
            Log.d(TAG, "before upload")

            if (alreadyUploaded(fileUrl)) {
                Log.d(TAG, "alreadyUploaded(fileUrl) in repo: ${alreadyUploaded(fileUrl)}")
                onResult.invoke(UiState.Success(fileUrl))
                return
            }
            val currentUserUid = auth.currentUser!!.uid
//            storageReference
//                .child("$DRIVER_DIRECTORY/$currentUserUid")
//                .putFile(fileUrl)
//                .await()
//            Log.d(TAG, "put file")

            val renamedImage = renameImageFile(fileUrl)
            val uri: Uri = withContext(Dispatchers.IO) {
                storageReference
                    .child("$DRIVER_DIRECTORY/$currentUserUid/$name")
                    .putFile(renamedImage)
                    .await()
                    .storage
                    .downloadUrl
                    .await()
            }
            Log.d(TAG, "after upload")
            onResult.invoke(UiState.Success(uri))

        } catch (e: FirebaseException) {
            Log.d(TAG, e.message.toString())
            onResult.invoke(UiState.Failure(e.message))
        } catch (e: Exception) {
            Log.d(TAG, e.message.toString())
            onResult.invoke(UiState.Failure(e.message))
        }
    }

    private fun renameImageFile(fileUrl: Uri): Uri {
        val originalFile = File(fileUrl.path!!)
        val renamedFile = File.createTempFile("image_", ".jpg")

        originalFile.copyTo(renamedFile, true)
        return Uri.fromFile(renamedFile)
    }

    override fun updateDriverLocations(
        pickup: RadeefLocation,
        destination: RadeefLocation,
        result: (UiState<Boolean>) -> Unit
    ) {
        val data = mapOf(
            "pickup" to pickup.toGeoPoint(),
            "pickup_title" to pickup.title,
            "destination" to destination.toGeoPoint(),
            "destination_title" to destination.title,
            // "status" to UserStatus.SEARCHING.value
        )
        logD(data.toString())

        database.collection(FirestoreTables.DRIVERS).document(auth.currentUser!!.uid)
            .update(data)
            .addOnSuccessListener {
                result(UiState.Success(true))
            }.addOnFailureListener {
                result(UiState.Failure(it.message))
            }
    }
}