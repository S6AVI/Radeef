package com.saleem.radeef.passenger.ui.profile

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
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
class ProfileFragment : Fragment(R.layout.fragment_profile) {
    lateinit var binding: FragmentProfileBinding
    val viewModel: ProfileViewModel by viewModels()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentProfileBinding.bind(view)

        binding.backBtn.setOnClickListener {
            findNavController().popBackStack()
        }


        binding.saveBtn.setOnClickListener {
            if (isDataValid()) {
                val name = binding.nameEt.text.toString()
                val email = binding.emailEt.text.toString()
                val gender = binding.genderAutoComplete.text.toString()
                val passenger = Passenger(name = name, email = email, gender = gender)

                viewModel.updatePassengerInfo(passenger)
            }
        }

        // observe update
        viewModel.update.observe(viewLifecycleOwner) { state ->
            when (state) {
                UiState.Loading -> {
                    binding.saveBtn.text = ""
                    binding.saveBtn.isEnabled = false
                    binding.progressBar.show()

                }

                is UiState.Success -> {
                    binding.progressBar.hide()
                    binding.saveBtn.isEnabled = false
                    binding.saveBtn.text = getString(R.string.save_changes)

                    toast(state.data)
                }

                is UiState.Failure -> {
                    binding.progressBar.hide()
                    binding.saveBtn.text = getString(R.string.save_changes)
                   toast(state.error.toString())
                }
            }
        }

        // fetch passenger data
        viewModel.getPassenger()


        // observe passenger data
        viewModel.passenger.observe(viewLifecycleOwner) { state ->
            when (state) {
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

    private fun observeFieldsChanges() {
        binding.emailEt.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable?) {
                binding.saveBtn.isEnabled = true
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}


        })

        binding.nameEt.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable?) {
                binding.saveBtn.isEnabled = true
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}


        })

        binding.genderAutoComplete.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable?) {
                binding.saveBtn.isEnabled = true
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        })
    }

    // validate data
    private fun isDataValid(): Boolean {
        var isValid = true

        if (binding.nameEt.text.toString().length < 4) {

            binding.nameIl.isErrorEnabled = true
            binding.nameIl.error = getString(R.string.error_name)
            isValid = false
        } else {
            binding.nameIl.isErrorEnabled = false
        }

        val email = binding.emailEt.text.toString()
        if (email.isNotEmpty() && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {

            binding.emailIl.isErrorEnabled = true
            binding.emailIl.error = getString(R.string.error_email)
            isValid = false
        } else {
            binding.emailIl.isErrorEnabled = false
        }
        return isValid
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
                }
            )

            val genderAdapter =
                ArrayAdapter(requireContext(), R.layout.dropdown_item, genders.subList(0, 2))
            binding.genderAutoComplete.setAdapter(genderAdapter)

            observeFieldsChanges()
        }
    }

    override fun onResume() {
        super.onResume()
        val genderAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, genders)
        binding.genderAutoComplete.setAdapter(genderAdapter)
    }
}



