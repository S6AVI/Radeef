package com.saleem.radeef.passenger.ui.profile

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.saleem.radeef.R
import com.saleem.radeef.data.model.Passenger

import com.saleem.radeef.databinding.FragmentProfileBinding
import com.saleem.radeef.util.TAG
import com.saleem.radeef.util.Gender
import com.saleem.radeef.util.UiState
import com.saleem.radeef.util.genders
import com.saleem.radeef.util.hide
import com.saleem.radeef.util.isValidEmail
import com.saleem.radeef.util.show
import com.saleem.radeef.util.toast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileFragment: Fragment(R.layout.fragment_profile) {
    lateinit var binding: FragmentProfileBinding
    val viewModel: ProfileViewModel by viewModels()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentProfileBinding.bind(view)

        binding.backBtn.setOnClickListener{
            findNavController().popBackStack()
        }


        binding.emailIl.isErrorEnabled = binding.emailEt.text.toString().isNotEmpty() &&
                binding.emailEt.text.toString().isValidEmail()


        binding.saveBtn.setOnClickListener {
            if (isDataValid()) {
                val name = binding.nameEt.text.toString()
                val email = binding.emailEt.text.toString()
                val gender = binding.genderAutoComplete.text.toString()

                Log.d(TAG,"name:$name\nemail:$email\ngender:$gender")

                val passenger = Passenger(name = name, email = email, gender = gender)
                viewModel.updatePassengerInfo(
                    passenger
                )
            }
        }

        // observe update
        viewModel.update.observe(viewLifecycleOwner) {state ->
            when(state) {
                UiState.Loading -> {
                    binding.saveBtn.text = ""
                    binding.progressBar.show()

                }
                is UiState.Success -> {
                    binding.progressBar.hide()
                    binding.saveBtn.text = getString(R.string.save_changes)

                    toast(state.data)
                }
                is UiState.Failure -> {
                    binding.progressBar.hide()
                    binding.saveBtn.text = getString(R.string.save_changes)
                    Log.d(TAG, state.error.toString())
                }
            }
        }

        // fetch passenger data
        viewModel.getPassenger()


        // observe passenger data
        viewModel.passenger.observe(viewLifecycleOwner) {state ->
            when(state) {
                UiState.Loading -> {
                    binding.constLayout.hide()
                    binding.progressBar.show()

                }
                is UiState.Failure -> {
                    binding.progressBar.hide()
                    binding.constLayout.show()
                    Log.d(TAG, state.error.toString())
                }

                is UiState.Success -> {
                    binding.progressBar.hide()
                    binding.constLayout.show()
                    fillFields(state.data)

                }
            }
        }
    }

    // validate data
    private fun isDataValid(): Boolean {
        return (!binding.nameIl.isErrorEnabled) ||
                (!binding.emailIl.isErrorEnabled) ||
                binding.genderAutoComplete.text.toString().isEmpty()
    }

    // bind values to fields
    private fun fillFields(passenger: Passenger) {
        binding.apply {
            nameEt.setText(passenger.name)
            phoneEt.setText(passenger.phoneNumber)
            emailEt.setText(passenger.email)
            genderAutoComplete.setText(
                if (passenger.gender == Gender.NONE.value) {
                    ""
                } else {
                    passenger.gender
                })

            val genderAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, genders.subList(0,2))
            binding.genderAutoComplete.setAdapter(genderAdapter)
        }
    }
    override fun onResume() {
        super.onResume()
        val genderAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, genders)
        binding.genderAutoComplete.setAdapter(genderAdapter)
    }
}



