package com.saleem.radeef.passenger.ui.auth

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.saleem.radeef.R
import com.saleem.radeef.databinding.FragmentOtpBinding
import com.saleem.radeef.ui.auth.OtpFragmentDirections
import com.saleem.radeef.util.UiState
import com.saleem.radeef.util.exhaustive
import com.saleem.radeef.util.hide
import com.saleem.radeef.util.show
import com.saleem.radeef.util.toast
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

                viewModel.signInWithPhoneAuthCredential(code)
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
                    binding.progressBar.show()
                }

                is UiState.Success -> {
                    binding.progressBar.hide()
                    toast(state.data)
                    val action =
                        OtpFragmentDirections.actionOtpFragmentToEnterNameFragment()
                    findNavController().navigate(action)
                }

                is UiState.Failure -> {
                    binding.progressBar.hide()
                    binding.verifyBtn.setText("Verify")
                    toast(state.error)
                }
            }.exhaustive

        }
    }


}