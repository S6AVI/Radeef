package com.saleem.radeef.driver.ui.profile

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.saleem.radeef.R
import com.saleem.radeef.data.model.Driver
import com.saleem.radeef.databinding.DriverFragmentProfileBinding

import com.saleem.radeef.util.TAG
import com.saleem.radeef.util.UiState
import com.saleem.radeef.util.genders
import com.saleem.radeef.util.hide
import com.saleem.radeef.util.isValidEmail
import com.saleem.radeef.util.show
import com.saleem.radeef.util.toast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DriverProfileFragment : Fragment(R.layout.driver_fragment_profile) {
    lateinit var binding: DriverFragmentProfileBinding
    val viewModel: DriverProfileViewModel by viewModels()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = DriverFragmentProfileBinding.bind(view)


        binding.base.backBtn.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.base.genderIl.isEnabled = false
        binding.base.nameIl.isEnabled = false

        binding.base.emailIl.isErrorEnabled = binding.base.emailEt.text.toString().isNotEmpty() &&
                binding.base.emailEt.text.toString().isValidEmail()


        binding.base.saveBtn.setOnClickListener {
            if (isDataValid()) {
                val driver = createDriver()
                viewModel.updateDriverInfo(
                    driver
                )

            }
        }

        viewModel.update.observe(viewLifecycleOwner) { state ->
            when (state) {
                UiState.Loading -> {
                    binding.base.saveBtn.text = ""
                    binding.base.progressBar.show()

                }

                is UiState.Success -> {
                    binding.base.progressBar.hide()
                    binding.base.saveBtn.text = getString(R.string.save_changes)

                    toast(state.data)
                }

                is UiState.Failure -> {
                    binding.base.progressBar.hide()
                    binding.base.saveBtn.text = getString(R.string.save_changes)
                    Log.d(TAG, state.error.toString())
                }
            }
        }

        viewModel.getDriver()

        viewModel.driver.observe(viewLifecycleOwner) { state ->
            when (state) {
                UiState.Loading -> {
                    binding.base.constLayout.hide()
                    binding.base.progressBar.show()

                }

                is UiState.Failure -> {
                    binding.base.progressBar.hide()
                    binding.base.constLayout.show()
                    Log.d(TAG, state.error.toString())
                }

                is UiState.Success -> {
                    loadImage(state.data.personalPhotoUrl)
                    binding.base.progressBar.hide()
                    binding.base.constLayout.show()
                    fillFields(state.data)

                }
            }
        }
    }
    private fun loadImage(personalPhotoUrl: String) {
        Glide.with(requireContext())
            .load(personalPhotoUrl)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .into(binding.base.profileImage)
    }

    private fun isDataValid(): Boolean {
        return (!binding.base.nameIl.isErrorEnabled) ||
                (!binding.base.emailIl.isErrorEnabled)

    }

    private fun fillFields(driver: Driver) {
        binding.base.apply {
            nameEt.setText(driver.name)
            phoneEt.setText(driver.phoneNumber)
            emailEt.setText(driver.email)
            genderAutoComplete.setText(driver.sex)
        }

    }


    private fun createDriver(): Driver {
        val driverTemp = viewModel.driverData!!
        return driverTemp.copy(
            email = binding.base.emailEt.text.toString()
        )
    }

    override fun onResume() {
        super.onResume()
        val genderAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, genders)
        binding.base.genderAutoComplete.setAdapter(genderAdapter)
    }
}



