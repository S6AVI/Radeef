package com.saleem.radeef.data.repository

import android.app.Activity
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
//    override val currentUser: FirebaseUser?
//        get() =

    override fun registerPassenger(
        passenger: Passenger,
        phone: String,
        activity: Activity,
        result: (UiState<String>) -> Unit
    ) {

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phone) // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(activity) // Activity (for callback binding)
            //.setCallbacks(callbacks) // OnVerificationStateChangedCallbacks
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
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    override fun updatePassengerInfo(passenger: Passenger, result: (UiState<String>) -> Unit) {

        val document = database.collection(FirestoreTables.PASSENGERS).document()
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


    override fun loginPassenger(passenger: Passenger, result: (UiState<String>) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun logout() {
        TODO("Not yet implemented")
    }


}
