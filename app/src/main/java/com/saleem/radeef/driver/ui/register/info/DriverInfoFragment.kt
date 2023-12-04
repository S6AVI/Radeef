package com.saleem.radeef.driver.ui.register.info

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.ArrayAdapter
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.github.dhaval2404.imagepicker.ImagePicker
import com.saleem.radeef.InfoNavigationDirections
import com.saleem.radeef.R
import com.saleem.radeef.data.model.Driver
import com.saleem.radeef.util.RegistrationStatus
import com.saleem.radeef.databinding.DriverInfoFragmentBinding
import com.saleem.radeef.util.Constants
import com.saleem.radeef.util.ImageFileNames.PERSONAL
import com.saleem.radeef.util.Gender
import com.saleem.radeef.util.UiState
import com.saleem.radeef.util.disable
import com.saleem.radeef.util.enable
import com.saleem.radeef.util.genders
import com.saleem.radeef.util.getCountries
import com.saleem.radeef.util.hide
import com.saleem.radeef.util.isPassed
import com.saleem.radeef.util.logD
import com.saleem.radeef.util.show
import com.saleem.radeef.util.toast
import com.saleem.radeef.util.updateRegistrationStatus
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DriverInfoFragment : Fragment(R.layout.driver_info_fragment) {
    private lateinit var binding: DriverInfoFragmentBinding
    val viewModel: DriverInfoViewModel by viewModels()
    private var selectedImageUri: Uri? = null

    private lateinit var preferences: SharedPreferences

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = DriverInfoFragmentBinding.bind(view)

        setAdapters()

        binding.cameraIcon.setOnClickListener {
            openImagePicker()
        }

        binding.continueBt.setOnClickListener {
            hideErrors()
            if (isValidData()) {
                hideErrors()
                binding.continueBt.disable()
                viewModel.onContinueClicked(selectedImageUri!!, PERSONAL)
            }
        }

        viewModel.driver.observe(viewLifecycleOwner) {state ->
            when (state) {
                UiState.Loading -> {
                    binding.baseLayout.hide()
                    binding.waitProgressBar.show()
                }
                is UiState.Success -> {
                    loadImage(state.data.personalPhotoUrl)
                    binding.baseLayout.show()
                    fillFields(state.data)
                }
                is UiState.Failure -> {
                    binding.baseLayout.show()
                    binding.waitProgressBar.hide()
                    toast(state.error.toString())
                }

            }
        }

        viewModel.uploadImage.observe(viewLifecycleOwner) {state ->
            when (state) {
                UiState.Loading -> {
                    binding.progressBar.show()
                    binding.continueBt.text = ""
                }
                is UiState.Success -> {
                   // binding.progressBar.hide()
                    val driver = createDriver(state.data)
                    viewModel.updateDriverInfo(driver)
                    logD(state.data.toString())

                }
                is UiState.Failure -> {
                    binding.progressBar.hide()
                    binding.continueBt.enable()
                    binding.continueBt.text = getString(R.string.continue_label)
                    toast(state.error.toString())
                }

            }
        }

        viewModel.updateDriver.observe(viewLifecycleOwner) { state ->
            when (state) {
                UiState.Loading -> {
                    binding.progressBar.show()
                }
                is UiState.Success -> {
                    binding.progressBar.hide()
                    logD(state.data)
                    updateRegistrationStatus(RegistrationStatus.LICENSE, requireActivity())
                    val action = DriverInfoFragmentDirections.actionDriverInfoFragmentToDriverLicenseFragment()
                    findNavController().navigate(action)
                }
                is UiState.Failure -> {
                    binding.progressBar.hide()
                    toast(state.error.toString())
                }

            }
        }
    }

    private fun hideErrors() {
        binding.nameInputLayout.isErrorEnabled = false
        binding.numberIdInputLayout.isErrorEnabled = false
        binding.genderInputLayout.isErrorEnabled = false
        binding.nationalityInputLayout.isErrorEnabled = false
        binding.emailInputLayout.isErrorEnabled = false
        binding.photoErrorTextView.hide()
    }

    private fun loadImage(personalPhotoUrl: String) {
        if (personalPhotoUrl.isNotEmpty()) {
            Glide.with(requireContext())
                .load(personalPhotoUrl)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(binding.photoImageView)
        } else {
            binding.photoImageView.setImageResource(R.drawable.account)
        }

    }

    private fun createDriver(uri: Uri): Driver {
        return Driver(
            name = binding.nameEt.text.toString(),
            email = binding.emailEt.text.toString(),
            identityNumber = binding.idEt.text.toString(),
            sex = binding.genderAutoComplete.text.toString(),
            nationality = binding.nationalityAutoComplete.text.toString(),
            personalPhotoUrl = uri.toString(),
            registrationStatus = RegistrationStatus.LICENSE.value
        )
    }

    private fun isValidData(): Boolean {

        var isValid = true

        if (selectedImageUri == null || selectedImageUri.toString().isEmpty()) {
            binding.photoErrorTextView.show()
            isValid = false
        }

        if (binding.nameEt.text.toString().length < 4) {

            binding.nameInputLayout.isErrorEnabled = true
            binding.nameInputLayout.error = getString(R.string.error_name)
            isValid = false
        }

        if (binding.idEt.text.toString().length != 10) {

            binding.numberIdInputLayout.isErrorEnabled = true
            binding.numberIdInputLayout.error = getString(R.string.error_id)
            isValid = false
        }

        if (binding.genderAutoComplete.text.toString().isEmpty()) {

            binding.genderInputLayout.error = getString(R.string.error_gender)
            isValid = false
        }


        if (binding.nationalityAutoComplete.text.toString().isEmpty()) {

            binding.nationalityInputLayout.error = getString(R.string.error_nationality)
            isValid = false
        }


        val email = binding.emailEt.text.toString()
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {

            binding.emailInputLayout.isErrorEnabled = true
            binding.emailInputLayout.error = getString(R.string.error_email)
            isValid = false
        }

        return isValid
    }


    private fun fillFields(driver: Driver) {
        binding.apply {
            nameEt.setText(driver.name)
            emailEt.setText(driver.email)
            idEt.setText(driver.identityNumber)
            genderAutoComplete.setText(
                if (driver.sex == Gender.NONE.value) {
                  ""
                } else {
                    driver.sex
                })
            nationalityAutoComplete.setText(driver.nationality)
            setAdapters()
            selectedImageUri = driver.personalPhotoUrl.toUri()
            binding.waitProgressBar.hide()
        }

    }


    private fun setAdapters() {
        val nationalityAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, getCountries())
        binding.nationalityAutoComplete.setAdapter(nationalityAdapter)


        val genderAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, genders.subList(0,2))
        binding.genderAutoComplete.setAdapter(genderAdapter)
    }


    private fun openImagePicker() {
        ImagePicker.with(this)
            .crop()
            .galleryOnly()
            .compress(1024)			//Final image size will be less than 1 MB(Optional)
            .maxResultSize(1080, 1080)	//Final image resolution will be less than 1080 x 1080(Optional)
            .start()
        
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            val uri: Uri = data?.data!!
            selectedImageUri = uri
            binding.photoImageView.setImageURI(uri)
        } else if (resultCode == ImagePicker.RESULT_ERROR) {
           toast(ImagePicker.RESULT_ERROR.toString())
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        preferences = requireActivity().getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        if (isRegistrationCompleted()) {
            val action = InfoNavigationDirections.actionGlobalHomeNavigation()
            findNavController().navigate(action)
        }
        else if (isPassed(preferences, RegistrationStatus.INFO.value)) {
            val action = DriverInfoFragmentDirections.actionDriverInfoFragmentToDriverLicenseFragment()
            findNavController().navigate(action)
        }
    }

    private fun isRegistrationCompleted(): Boolean {
        return preferences.getString(Constants.CURRENT_SCREEN, null) == RegistrationStatus.COMPLETED.value
    }

}