package com.saleem.radeef.driver.ui.auth

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.saleem.radeef.AuthNavigationDirections
import com.saleem.radeef.R
import com.saleem.radeef.data.model.Driver
import com.saleem.radeef.databinding.FragementEnterNumberBinding

import com.saleem.radeef.util.UiState
import com.saleem.radeef.util.disable
import com.saleem.radeef.util.enable
import com.saleem.radeef.util.exhaustive
import com.saleem.radeef.util.hide
import com.saleem.radeef.util.hideKeyboard
import com.saleem.radeef.util.logD
import com.saleem.radeef.util.show
import com.saleem.radeef.util.toast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DriverEnterNumberFragment : Fragment(R.layout.fragement_enter_number) {
    lateinit var binding: FragementEnterNumberBinding
    val viewModel: DriverAuthViewModel by viewModels()

    private lateinit var phoneNumber: String

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragementEnterNumberBinding.bind(view)

        observer()
        binding.registerButton.setOnClickListener {
            phoneNumber = binding.phoneEt.text?.trim().toString()
            if (phoneNumber.isNotEmpty() && phoneNumber.length == 9) {
                hideKeyboard()
                binding.textInputLayout.isErrorEnabled = false
                val countryCode = binding.countryCodePicker.selectedCountryCode
                phoneNumber = getString(R.string.phone_number_format, countryCode, phoneNumber)
                viewModel.register(
                    Driver(phoneNumber = phoneNumber),
                    phoneNumber,
                    requireActivity()
                )
            } else {
                binding.textInputLayout.error = getString(R.string.error_phone)
            }

        }

    }

    private fun observer() {
        viewModel.register.observe(viewLifecycleOwner) { state ->
            when (state) {
                UiState.Loading -> {
                    binding.registerButton.text = ""
                    binding.registerButton.disable()
                    binding.progressBar.show()
                }

                is UiState.Success -> {
                    binding.progressBar.hide()
                    binding.registerButton.text = getString(R.string.continue_label)
                    val action =
                        DriverEnterNumberFragmentDirections.actionDriverEnterNumberFragmentToDriverOtpFragment()
                    findNavController().navigate(action)

                }

                is UiState.Failure -> {
                    binding.progressBar.hide()
                    binding.registerButton.enable()
                    binding.registerButton.setText(R.string.send_code)
                    logD(state.error.toString())
                    toast(state.error)
                }
            }.exhaustive

        }
    }

    private fun navigateToInfoGraph() {
        val action = AuthNavigationDirections.actionGlobalInfoNavigation()
        findNavController().navigate(action)
    }
    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (viewModel.isRegistered()) {
            navigateToInfoGraph()
        }
    }


}