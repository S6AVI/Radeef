package com.saleem.radeef.ui.enternumber

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.saleem.radeef.R
import com.saleem.radeef.databinding.FragementEnterNumberBinding
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class EnterNumberFragment : Fragment(R.layout.fragement_enter_number) {
    lateinit var binding: FragementEnterNumberBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var phoneNumber: String

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragementEnterNumberBinding.bind(view)
        auth = FirebaseAuth.getInstance()


        binding.registerButton.setOnClickListener {
            phoneNumber = binding.phoneEt.text?.trim().toString()
            if (phoneNumber.isNotEmpty() && phoneNumber.length == 9) {
                phoneNumber = "+${binding.countryCodePicker.selectedCountryCode}$phoneNumber"


                val options = PhoneAuthOptions.newBuilder(auth)
                    .setPhoneNumber(phoneNumber) // Phone number to verify
                    .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                    .setActivity(requireActivity()) // Activity (for callback binding)
                    .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks
                    .build()
                PhoneAuthProvider.verifyPhoneNumber(options)
            }

        }

    }

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            // This callback will be invoked in two situations:
            // 1 - Instant verification. In some cases the phone number can be instantly
            //     verified without needing to send or enter a verification code.
            // 2 - Auto-retrieval. On some devices Google Play services can automatically
            //     detect the incoming verification SMS and perform verification without
            //     user action.
            //Log.d(TAG, "onVerificationCompleted:$credential")
            signInWithPhoneAuthCredential(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            // This callback is invoked in an invalid request for verification is made,
            // for instance if the the phone number format is not valid.
            //Log.w(TAG, "onVerificationFailed", e)

            if (e is FirebaseAuthInvalidCredentialsException) {
                Toast.makeText(requireContext(), "CODE Problem", Toast.LENGTH_LONG).show()
                // Invalid request
                Log.d("Savi", "firebase invalid")
            } else if (e is FirebaseTooManyRequestsException) {
                // The SMS quota for the project has been exceeded
                Log.d("Savi", "firebase invalid")
            }
//            } else if (e is FirebaseAuthMissingActivityForRecaptchaException) {
//                // reCAPTCHA verification attempted with null Activity
//            }

            // Show a message and update the UI
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken,
        ) {

            Toast.makeText(requireContext(), "CODE SENT", Toast.LENGTH_LONG).show()
//
            val action = EnterNumberFragmentDirections.actionEnterNumberFragmentToOtpFragment(
                verificationId, phoneNumber
            )
            findNavController().navigate(action)

        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(requireContext(), "success!", Toast.LENGTH_LONG)
                    // Sign in success, update UI with the signed-in user's information
                    //  Log.d(TAG, "signInWithCredential:success")

                    val user = task.result?.user
                } else {
                    // Sign in failed, display a message and update the UI
                    //  Log.w(TAG, "signInWithCredential:failure", task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                    }
                    // Update UI
                }
            }
    }

    override fun onStart() {
        super.onStart()
        if (auth.currentUser != null) {
            val action = EnterNumberFragmentDirections.actionEnterNumberFragmentToEnterNameFragment()
            findNavController().navigate(action)
        }
    }

}