package com.saleem.radeef.passenger.ui.auth

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.saleem.radeef.PassengerAuthNavigationDirections
import com.saleem.radeef.R
import com.saleem.radeef.data.model.Passenger
import com.saleem.radeef.databinding.FragmentOtpBinding
import com.saleem.radeef.util.UiState
import com.saleem.radeef.util.disable
import com.saleem.radeef.util.enable
import com.saleem.radeef.util.exhaustive
import com.saleem.radeef.util.hide
import com.saleem.radeef.util.hideKeyboard
import com.saleem.radeef.util.logD
import com.saleem.radeef.util.show
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OtpFragment : Fragment(R.layout.fragment_otp) {
    private lateinit var binding: FragmentOtpBinding
    val viewModel: RegisterViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentOtpBinding.bind(view)
        val phone = arguments?.getString("phone")
        observer()

        binding.verifyBtn.setOnClickListener {
            val code = binding.otpEditText.text.toString()
            if (code.isNotEmpty() && code.length == 6) {
                hideKeyboard()
                binding.otpInputLayout.error = ""
                binding.otpInputLayout.isErrorEnabled = false
                viewModel.signInWithPhoneAuthCredential(code)
            } else {
                binding.otpInputLayout.error = getString(R.string.error_otp_length)
            }
        }
        binding.resendTv.setOnClickListener {
            viewModel.resendCode(requireActivity())
        }
    }

    private fun observer() {
        viewModel.verify.observe(viewLifecycleOwner) { state ->
            when (state) {
                UiState.Loading -> {
                    binding.verifyBtn.setText("")
                    binding.verifyBtn.disable()
                    binding.progressBar.show()
                }

                is UiState.Success -> {
                    binding.progressBar.hide()
                    logD(state.data.toString())
                    decideSection(state.data)
                }

                is UiState.Failure -> {
                    binding.progressBar.hide()
                    binding.verifyBtn.enable()
                    binding.verifyBtn.setText(R.string.verify)
                    binding.otpInputLayout.error = getString(R.string.error_otp)
                    logD(state.error.toString())
                }
            }.exhaustive

        }
    }

    private fun decideSection(passenger: Passenger) {
        if (passenger.name.isNotEmpty()) {
            val action =
                PassengerAuthNavigationDirections.actionGlobalPassengerInfoNavigation()
            findNavController().navigate(action)
        } else {
            val action =
                PassengerAuthNavigationDirections.actionGlobalPassengerInfoNavigation()
            findNavController().navigate(action)
        }
    }

}