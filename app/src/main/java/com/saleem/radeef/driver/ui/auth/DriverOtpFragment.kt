package com.saleem.radeef.driver.ui.auth

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.saleem.radeef.AuthNavigationDirections
import com.saleem.radeef.R
import com.saleem.radeef.util.RegistrationStatus
import com.saleem.radeef.databinding.FragmentOtpBinding
import com.saleem.radeef.util.UiState
import com.saleem.radeef.util.disable
import com.saleem.radeef.util.enable
import com.saleem.radeef.util.exhaustive
import com.saleem.radeef.util.hide
import com.saleem.radeef.util.hideKeyboard
import com.saleem.radeef.util.logD
import com.saleem.radeef.util.show
import com.saleem.radeef.util.updateRegistrationStatus
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DriverOtpFragment : Fragment(R.layout.fragment_otp) {
    private lateinit var binding: FragmentOtpBinding
    val viewModel: DriverAuthViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentOtpBinding.bind(view)


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
                    binding.verifyBtn.text = ""
                    binding.verifyBtn.disable()
                    binding.progressBar.show()
                }

                is UiState.Success -> {
                    binding.progressBar.hide()
                    logD(state.data.toString())
                    decideSection(state.data.registrationStatus)
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

    private fun decideSection(status: String) {
        updateRegistrationStatus(status, requireActivity())
        when(status) {
            RegistrationStatus.COMPLETED.value -> {
                val action = AuthNavigationDirections.actionGlobalHomeNavigation2()
                findNavController().navigate(action)
            }
            else -> {

                val action = AuthNavigationDirections.actionGlobalInfoNavigation()
                findNavController().navigate(action)
            }
        }.exhaustive
    }


}