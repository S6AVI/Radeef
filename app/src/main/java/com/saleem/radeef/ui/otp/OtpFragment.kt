package com.saleem.radeef.ui.otp

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.saleem.radeef.R
import com.saleem.radeef.ui.enternumber.RegisterViewModel
import com.saleem.radeef.databinding.FragmentOtpBinding
import com.saleem.radeef.util.UiState
import com.saleem.radeef.util.exhaustive
import com.saleem.radeef.util.toast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OtpFragment : Fragment(R.layout.fragment_otp) {
    private lateinit var binding: FragmentOtpBinding
    val viewModel: RegisterViewModel by viewModels()

    //private lateinit var auth: FirebaseAuth

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentOtpBinding.bind(view)
        val phone = arguments?.getString("phone")
        observer()

        binding.verifyBtn.setOnClickListener {
            val code = binding.otpEditText.text.toString()
            if (code.isNotEmpty() && code.length == 6) {

//                val credential: PhoneAuthCredential = PhoneAuthProvider.getCredential(
//                    verId!!, code
//                )

                viewModel.signInWithPhoneAuthCredential(code)
            } else {
                //Toast.makeText(requireContext(), "stuck here!", Toast.LENGTH_LONG)
            }


        }
    }

    private fun observer() {
        viewModel.verify.observe(viewLifecycleOwner) { state ->
            when (state) {
                UiState.Loading -> {}
                is UiState.Success -> {
                    toast(state.data)
                    val action = OtpFragmentDirections.actionOtpFragmentToEnterNameFragment()
                    findNavController().navigate(action)
                }

                is UiState.Failure -> toast(state.error)
            }.exhaustive

        }
    }


}