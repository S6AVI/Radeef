package com.saleem.radeef.ui.otp

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.saleem.radeef.R
import com.saleem.radeef.databinding.FragmentOtpBinding
import com.saleem.radeef.ui.enternumber.EnterNumberFragmentDirections
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OtpFragment: Fragment(R.layout.fragment_otp) {
    private lateinit var binding: FragmentOtpBinding
    private lateinit var auth: FirebaseAuth

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentOtpBinding.bind(view)
        auth = FirebaseAuth.getInstance()
        val verId = arguments?.getString("verId")
        val phone = arguments?.getString("phone")

//        binding.verifyBtn.setOnClickListener {
//            Toast.makeText(requireContext(), "above here!", Toast.LENGTH_LONG)
//        }
        binding.verifyBtn.setOnClickListener {
            Toast.makeText(requireContext(), "above here!", Toast.LENGTH_LONG)
            val code = binding.otpEditText.text.toString()
            if (code.isNotEmpty() && code.length == 6) {
                val credential: PhoneAuthCredential = PhoneAuthProvider.getCredential(
                    verId!!, code
                )
                signInWithPhoneAuthCredential(credential)
            } else {
                Toast.makeText(requireContext(), "stuck here!", Toast.LENGTH_LONG)
            }


        }
    }


    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    //  Log.d(TAG, "signInWithCredential:success")
//                    Toast.makeText(requireContext(), "success!", Toast.LENGTH_LONG)
//                    val action = OtpFragmentDirections.actionOtpFragmentToWelcomeFragment2()
//                    findNavController().navigate(action)


                    val user = task.result?.user


                    val action = OtpFragmentDirections.actionOtpFragmentToEnterNameFragment()
                    findNavController().navigate(action)

                } else {
                    // Sign in failed, display a message and update the UI
                    //  Log.w(TAG, "signInWithCredential:failure", task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                        Toast.makeText(requireContext(), "sume bugs!", Toast.LENGTH_LONG)
                    }
                    // Update UI
                }
            }
    }

}