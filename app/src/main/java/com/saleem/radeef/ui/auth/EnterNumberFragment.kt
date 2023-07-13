package com.saleem.radeef.ui.auth

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.saleem.radeef.R
import com.saleem.radeef.data.firestore.Passenger
import com.saleem.radeef.databinding.FragementEnterNumberBinding

import com.saleem.radeef.util.UiState
import com.saleem.radeef.util.exhaustive
import com.saleem.radeef.util.hide
import com.saleem.radeef.util.show
import com.saleem.radeef.util.toast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EnterNumberFragment : Fragment(R.layout.fragement_enter_number) {
    lateinit var binding: FragementEnterNumberBinding
    val viewModel: RegisterViewModel by viewModels()

    private lateinit var phoneNumber: String

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragementEnterNumberBinding.bind(view)



        observer()


        binding.registerButton.setOnClickListener {
            phoneNumber = binding.phoneEt.text?.trim().toString()
            if (phoneNumber.isNotEmpty() && phoneNumber.length == 9) {
                phoneNumber = "+${binding.countryCodePicker.selectedCountryCode}$phoneNumber"
                viewModel.register(
                    createPassenger(),
                    phoneNumber,
                    requireActivity()
                )
            }

        }

    }

    private fun observer() {
        viewModel.register.observe(viewLifecycleOwner) { state ->
                when(state) {
                    UiState.Loading -> {
                        binding.registerButton.setText("")
                        binding.progressBar.show()
                    }
                    is UiState.Success -> {
                        binding.progressBar.hide()
                        //binding.registerButton.setText("Continue")
                        toast(state.data)
                        val action = EnterNumberFragmentDirections.actionEnterNumberFragmentToOtpFragment(
                                phoneNumber
                            )
                        findNavController().navigate(action)
                    }
                    is UiState.Failure -> {
                        binding.progressBar.hide()
                        binding.registerButton.setText("Continue")
                        toast(state.error)
                    }
                }.exhaustive

        }
    }

    private fun createPassenger(): Passenger {
        return Passenger(
            phoneNumber = phoneNumber
        )
    }

//    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
//
//        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
//
//            signInWithPhoneAuthCredential(credential)
//        }
//
//        override fun onVerificationFailed(e: FirebaseException) {
//
//            if (e is FirebaseAuthInvalidCredentialsException) {
//                Toast.makeText(requireContext(), "CODE Problem", Toast.LENGTH_LONG).show()
//                // Invalid request
//                Log.d("Savi", "firebase invalid")
//            } else if (e is FirebaseTooManyRequestsException) {
//                // The SMS quota for the project has been exceeded
//                Log.d("Savi", "firebase invalid")
//            }
//        }
//
//        override fun onCodeSent(
//            verificationId: String,
//            token: PhoneAuthProvider.ForceResendingToken,
//        ) {
//
//            Toast.makeText(requireContext(), "CODE SENT", Toast.LENGTH_LONG).show()
////
//            val action = EnterNumberFragmentDirections.actionEnterNumberFragmentToOtpFragment(
//                verificationId, phoneNumber
//            )
//            findNavController().navigate(action)
//
//        }
//    }
//
//    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
//        auth.signInWithCredential(credential)
//            .addOnCompleteListener(requireActivity()) { task ->
//                if (task.isSuccessful) {
//                    Toast.makeText(requireContext(), "success!", Toast.LENGTH_LONG)
//
//                    val user = task.result?.user
//                } else {
//
//                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
//                    }
//
//                }
//            }
//    }

    override fun onStart() {
        super.onStart()
        if (viewModel.isRegistered()) {
            val action = EnterNumberFragmentDirections.actionEnterNumberFragmentToHomeFragment()
            findNavController().navigate(action)
        }

    }
//    override fun onStart() {
//        super.onStart()
//        if (auth.currentUser != null) {
//            val action =
//                EnterNumberFragmentDirections.actionEnterNumberFragmentToEnterNameFragment()
//            findNavController().navigate(action)
//        }
//    }

}