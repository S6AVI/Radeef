package com.saleem.radeef.data.impl

import android.app.Activity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.saleem.radeef.data.model.Driver
import com.saleem.radeef.data.model.Passenger
import com.saleem.radeef.data.repository.AuthRepository
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

    // start authenticating; send OTP code to user
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
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    auth.signInWithCredential(credential)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                passenger.passengerID = task.result.user?.uid ?: ""
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
                    result.invoke(UiState.Success("Verification code sent"))
                }
            })
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    // update passenger info; create a document if it's a new passenger (for auth)
    override fun updatePassengerInfo(passenger: Passenger, result: (UiState<Passenger>) -> Unit) {
        val documentCollection = database.collection(FirestoreTables.PASSENGERS)
        documentCollection
            .whereEqualTo("phoneNumber", passenger.phoneNumber)
            .get()
            .addOnSuccessListener {
                if (it.documents.isNotEmpty()) {
                    val retrievedPassenger = it.documents[0].toObject(Passenger::class.java)
                    if (retrievedPassenger != null) {
                        result.invoke(
                            UiState.Success(retrievedPassenger)
                        )
                    }

                } else {
                    documentCollection.document(passenger.passengerID)
                        .set(passenger)
                        .addOnSuccessListener {
                            result.invoke(
                                UiState.Success(passenger)
                            )
                        }
                        .addOnFailureListener { error ->
                            result.invoke(
                                UiState.Failure(
                                    error.localizedMessage
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


    // check if code matches; and sign in user to the app
    override fun verifyCode(code: String, result: (UiState<Passenger>) -> Unit) {
        val credential = PhoneAuthProvider.getCredential(verificationId, code)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    passenger.passengerID = task.result?.user?.uid ?: ""
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


    // store name of passenger
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

    // resend code again
    override fun resendCode(activity: Activity, result: (UiState<String>) -> Unit) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(passenger.phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {

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


    // check if driver is already registered
    override fun isRegistered(): Boolean {
        return auth.currentUser != null
    }

    // check if passenger has name already (registered)
    override fun hasName(callback: (Boolean) -> Unit) {
        val id = getUserId()
        if (id == null) {
            callback.invoke(false)
        } else {
            database.collection(FirestoreTables.PASSENGERS).document(id)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val name = document.getString("name") ?: ""
                        callback.invoke(name.isNotEmpty())
                    } else {
                        callback.invoke(false)
                    }
                }
                .addOnFailureListener {
                    callback.invoke(false)
                }
        }
    }


    // logout
    override fun logout(result: (UiState<String>) -> Unit) {
        auth.signOut()
        result.invoke(UiState.Success("user signed out"))
    }


    // get passenger; for profile
    override fun getPassenger(result: (UiState<Passenger>) -> Unit) {
        val id = getUserId()
        if (id != null) {
            database.collection(FirestoreTables.PASSENGERS).document(id)
                .get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val passenger = documentSnapshot.toObject(Passenger::class.java)
                        if (passenger != null) {
                            result.invoke(UiState.Success(passenger))
                        } else {
                            result.invoke(UiState.Failure("Failed to retrieve passenger data"))
                        }
                    } else {
                        result.invoke(UiState.Failure("Passenger document does not exist"))
                    }
                }
                .addOnFailureListener { exception ->
                    result.invoke(UiState.Failure(exception.message.toString()))
                }
        } else {
            result.invoke(UiState.Failure("User ID is null"))
        }
    }



    // check if phone number is associated already with a driver
    override fun isPhoneNumberAssociatedWithDriver(phone: String, callback: (Boolean) -> Unit) {
        val driversCollection = database.collection(FirestoreTables.DRIVERS)

        val query = driversCollection.whereEqualTo("phoneNumber", phone)

        query.get().addOnSuccessListener { querySnapshot ->
            val drivers = querySnapshot.toObjects(Driver::class.java)
            if (drivers.isNotEmpty()) {
                callback(true)
            } else {
                callback(false)
            }
        }.addOnFailureListener {
            callback(false)
        }
    }

    // helper
    private fun getUserId() = auth.currentUser?.uid
}
