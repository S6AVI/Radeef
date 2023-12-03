package com.saleem.radeef.passenger.ui.auth

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.saleem.radeef.PassengerAuthNavigationDirections
import com.saleem.radeef.R
import com.saleem.radeef.data.model.Passenger
import com.saleem.radeef.databinding.FragementEnterNumberBinding
import com.saleem.radeef.util.UiState
import com.saleem.radeef.util.disable
import com.saleem.radeef.util.enable
import com.saleem.radeef.util.exhaustive
import com.saleem.radeef.util.hide
import com.saleem.radeef.util.hideKeyboard
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
                hideKeyboard()
                binding.textInputLayout.error = ""
                binding.textInputLayout.isErrorEnabled = false
                phoneNumber = "+${binding.countryCodePicker.selectedCountryCode}$phoneNumber"
                viewModel.register(
                    createPassenger(),
                    phoneNumber,
                    requireActivity()
                )
            } else {
                binding.textInputLayout.error = getString(R.string.error_phone)
                //binding.textInputLayout.isErrorEnabled = true
            }

        }

    }

    private fun observer() {
        viewModel.register.observe(viewLifecycleOwner) { state ->
                when(state) {
                    UiState.Loading -> {
                        binding.registerButton.setText("")
                        binding.registerButton.disable()
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
                        binding.registerButton.enable()
                        binding.registerButton.setText(R.string.send_code)
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


    override fun onStart() {
        super.onStart()
        if (viewModel.isRegistered()) {
            val action = PassengerAuthNavigationDirections.actionGlobalPassengerInfoNavigation()
            findNavController().navigate(action)
        }

    }

}