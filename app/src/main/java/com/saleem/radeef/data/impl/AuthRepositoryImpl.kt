package com.saleem.radeef.data.impl

import android.app.Activity
import android.util.Log
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.saleem.radeef.data.model.Passenger
import com.saleem.radeef.data.model.Driver
import com.saleem.radeef.data.repository.AuthRepository
import com.saleem.radeef.passenger.ui.home.TAG
import com.saleem.radeef.util.FirestoreTables
import com.saleem.radeef.util.UiState
import com.saleem.radeef.util.logD
import java.util.concurrent.TimeUnit

class AuthRepositoryImpl(
    val database: FirebaseFirestore,
    private val auth: FirebaseAuth
) : AuthRepository {
    private lateinit var verificationId: String
    private lateinit var passenger: Passenger
    private lateinit var token: PhoneAuthProvider.ForceResendingToken
//    override val currentUser: FirebaseUser?
//        get() =

    private fun getUserId() = auth.currentUser?.uid
    override fun isPhoneNumberAssociatedWithDriver(phone: String, callback: (Boolean) -> Unit) {
        val driversCollection = database.collection(FirestoreTables.DRIVERS)

        val query = driversCollection.whereEqualTo("phoneNumber", phone)

        query.get().addOnSuccessListener { querySnapshot ->
            val drivers = querySnapshot.toObjects(Driver::class.java)
            if (drivers.isNotEmpty()) {
                // Phone number is associated with a driver
                callback(true)
            } else {
                // Phone number is not associated with any driver
                callback(false)
            }
        }.addOnFailureListener { exception ->
            // Handle any errors that occurred while retrieving data
            callback(false)
        }
    }

    override fun registerPassenger(
        passenger: Passenger,
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
                    //signIn(passenger, credential, result)
                    Log.d(TAG, "registerPassenger: onverifcomp: line 41")
                    auth.signInWithCredential(credential)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                passenger.passengerID = task.result.user?.uid ?: ""
                                logD("passenger ID: ${passenger.passengerID}")
                                updatePassengerInfo(passenger) { state ->
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
                    this@AuthRepositoryImpl.passenger = passenger
                    this@AuthRepositoryImpl.verificationId = verificationId
                    this@AuthRepositoryImpl.token = token
                    logD("verification code sent!")
                    result.invoke(UiState.Success("Verification code sent"))
                }
            })
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    override fun updatePassengerInfo(passenger: Passenger, result: (UiState<Passenger>) -> Unit) {
        val documentCollection = database.collection(FirestoreTables.PASSENGERS)
        documentCollection
            .whereEqualTo("phoneNumber", passenger.phoneNumber)
            .get()
            .addOnSuccessListener {
                if (it.documents.isNotEmpty()) {
                    logD("document: ${it.documents.get(0)}")
                    val retrievedPassenger = it.documents[0].toObject(Passenger::class.java)
                    if (retrievedPassenger != null) {
                        result.invoke(
                            UiState.Success(retrievedPassenger)
                        )
                    }

                } else {
                    logD("passenger: $passenger")
                    documentCollection.document(passenger.passengerID)
                        .set(passenger)
                        .addOnSuccessListener {
                            result.invoke(
                                UiState.Success(passenger)
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


    override fun signIn(code: String, result: (UiState<Passenger>) -> Unit) {
        val credential = PhoneAuthProvider.getCredential(verificationId, code)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    passenger.passengerID = task.result?.user?.uid ?: ""
                    logD("signIn: ${passenger.passengerID}")
                    updatePassengerInfo(passenger) { state ->
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
        val id = auth.currentUser?.uid
        if (id != null) {
            val passengerRef = database.collection(FirestoreTables.PASSENGERS).document(id)
            passengerRef
                .update("name", name)
                .addOnSuccessListener {
                    result.invoke(UiState.Success("Name updated successfully"))
                }
                .addOnFailureListener { exception ->
                    result.invoke(UiState.Failure("Failed to update name: ${exception.message}"))
                }
        } else {
            result.invoke(UiState.Failure("User ID is null"))
        }
    }

    override fun resendCode(activity: Activity, result: (UiState<String>) -> Unit) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(passenger.phoneNumber)
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
                    this@AuthRepositoryImpl.verificationId = verificationId
                    result.invoke(UiState.Success("Verification code sent"))
                }
            })
            .setForceResendingToken(token)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    override fun isRegistered(): Boolean {
        return auth.currentUser != null
    }

    override fun hasName(callback: (Boolean) -> Unit) {
        val id = getUserId()
        if (id == null) {
            callback.invoke(false)
        } else {
            val db = database.collection(FirestoreTables.PASSENGERS).document(id)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val name = document.getString("name") ?: ""
                        callback.invoke(name.isNotEmpty())
                    } else {
                        callback.invoke(false)
                    }
                }
                .addOnFailureListener { exception ->
                    // Handle any exceptions that occurred while retrieving the document
                    callback.invoke(false)
                }
        }
    }


    override fun logout(result: (UiState<String>) -> Unit) {
        auth.signOut()
        result.invoke(UiState.Success("user signed out"))
    }


    override fun getName(result: (UiState<String>) -> Unit) {
        var name: String = ""
        database.collection(FirestoreTables.PASSENGERS)
            .whereEqualTo("passengerID", auth.currentUser?.uid)
            .limit(1)
            .get()
            .addOnSuccessListener {
                if (!it.isEmpty) {
                    val documentSnapshot = it.documents[0]
                    name = documentSnapshot.getString("name") ?: ""
                    result.invoke(
                        UiState.Success(name)
                    )
                } else {

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

    override fun getPassenger(result: (UiState<Passenger>) -> Unit) {
        database.collection(FirestoreTables.PASSENGERS)
            .whereEqualTo("passengerID", auth.currentUser?.uid)
            .limit(1)
            .get()
            .addOnSuccessListener {
                if (!it.isEmpty) {
                    val documentSnapshot = it.documents[0]
                    val passenger = documentSnapshot.toObject(Passenger::class.java)!!
                    result.invoke(
                        UiState.Success(passenger)
                    )
                } else {

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
}