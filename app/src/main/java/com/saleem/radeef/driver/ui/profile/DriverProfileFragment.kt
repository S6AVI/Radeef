package com.saleem.radeef.driver.ui.profile

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
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
import com.saleem.radeef.util.UiState
import com.saleem.radeef.util.disable
import com.saleem.radeef.util.genders
import com.saleem.radeef.util.hide
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

        binding.base.genderIl.disable()
        binding.base.nameIl.disable()

        binding.base.saveBtn.isEnabled = false

        binding.base.emailEt.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable?) {
                val email = viewModel.driverData?.email
                if (email != null) {
                    binding.base.saveBtn.isEnabled = s.toString() != email
                }

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }


        })
        binding.base.saveBtn.setOnClickListener {
            if (isDataValid()) {
                binding.base.saveBtn.isEnabled = false
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
                    binding.base.saveBtn.isEnabled = false
                    binding.base.saveBtn.text = getString(R.string.save_changes)

                    toast(state.data)
                }

                is UiState.Failure -> {
                    binding.base.progressBar.hide()
                    binding.base.saveBtn.isEnabled = true
                    binding.base.saveBtn.text = getString(R.string.save_changes)
                    toast(state.error.toString())
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
                    toast(state.error.toString())
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
        val email = binding.base.emailEt.text.toString()
        return if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.base.emailIl.isErrorEnabled = false
            true
        } else {
            binding.base.emailIl.error = getString(R.string.error_email)
            false
        }
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



