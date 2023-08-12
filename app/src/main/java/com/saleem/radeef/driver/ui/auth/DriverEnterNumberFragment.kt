package com.saleem.radeef.driver.ui.auth

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.saleem.radeef.R
import com.saleem.radeef.data.firestore.Passenger
import com.saleem.radeef.data.firestore.driver.Driver
import com.saleem.radeef.databinding.FragementEnterNumberBinding

import com.saleem.radeef.util.UiState
import com.saleem.radeef.util.exhaustive
import com.saleem.radeef.util.hide
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
                phoneNumber = "+${binding.countryCodePicker.selectedCountryCode}$phoneNumber"
                viewModel.register(
                    createDriver(),
                    phoneNumber,
                    requireActivity()
                )
            }

        }

    }

    private fun observer() {
        viewModel.register.observe(viewLifecycleOwner) { state ->
            when (state) {
                UiState.Loading -> {
                    binding.registerButton.setText("")
                    binding.progressBar.show()
                }

                is UiState.Success -> {
                    binding.progressBar.hide()
                    binding.registerButton.setText("Continue")
                    //binding.registerButton.setText("Continue")
                    toast(state.data)
                    val action = DriverEnterNumberFragmentDirections.actionDriverEnterNumberFragmentToDriverOtpFragment()
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

    private fun createDriver(): Driver {
        return Driver(
            phoneNumber = phoneNumber
        )
    }


}