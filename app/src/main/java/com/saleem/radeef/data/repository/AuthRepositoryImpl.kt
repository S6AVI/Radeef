package com.saleem.radeef.data.repository

import android.app.Activity
import android.util.Log
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.saleem.radeef.data.firestore.Passenger
import com.saleem.radeef.util.FirestoreTables
import com.saleem.radeef.util.UiState
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

    override fun updatePassengerInfo(passenger: Passenger, result: (UiState<String>) -> Unit) {

        val document = if (passenger.passengerID.isNotEmpty()) {
            database.collection(FirestoreTables.PASSENGERS).document(passenger.passengerID)


        } else {
            database.collection(FirestoreTables.PASSENGERS).document()
        }


        passenger.passengerID = document.id
        document
            .set(passenger)
            .addOnSuccessListener {
                result.invoke(
                    UiState.Success("Passenger has been updated")
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


    override fun signIn(code: String, result: (UiState<String>) -> Unit) {
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

    override fun updateName(name: String, result: (UiState<String>) -> Unit) {
        updatePassengerInfo(passenger.copy(name = name)) { state ->
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
    }



    //val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks()

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

    override fun logout() {
        auth.signOut()
    }


}
