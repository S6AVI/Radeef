package com.saleem.radeef.driver.ui.register.info

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.github.dhaval2404.imagepicker.ImagePicker
//import com.github.drjacky.imagepicker.ImagePicker
import com.saleem.radeef.R
import com.saleem.radeef.data.firestore.driver.Driver
import com.saleem.radeef.data.firestore.driver.RegistrationStatus
import com.saleem.radeef.databinding.DriverInfoFragmentBinding
import com.saleem.radeef.util.Constants.CROP_IMAGE_REQUEST
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
import com.yalantis.ucrop.UCrop
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

@AndroidEntryPoint
class DriverInfoFragment() : Fragment(R.layout.driver_info_fragment) {
    private lateinit var binding: DriverInfoFragmentBinding
    val viewModel: DriverInfoViewModel by viewModels()
    private var selectedImageUri: Uri? = null

    private lateinit var preferences: SharedPreferences
//        requireActivity().getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)


//    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
//        uri?.let {
//            imageView.setImageURI(uri)
//            //viewModel.setSelectedImageUri(uri)
//        }
//    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        val currentPage = preferences.getString("currentStatus", RegistrationStatus.INFO.value)
////        if (currentPage != RegistrationStatus.INFO.value) {
////
////        }
        binding = DriverInfoFragmentBinding.bind(view)


        setAdapters()

        binding.cameraIcon.setOnClickListener {
            openImagePicker()
            toast("icon clicked")
        }

        binding.continueBt.setOnClickListener {
            //Log.d(TAG, isValidDate().toString())
            if (isValidDate()) {
                binding.continueBt.disable()
                viewModel.onContinueClicked(selectedImageUri!!, "personal")
                //toast("correct data")
            } else {
                toast("Missing required data")
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
                    binding.continueBt.setText("")
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
                    binding.continueBt.setText(getString(R.string.continue_label))
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
                    logD(state.data.toString())
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



    private fun loadImage(personalPhotoUrl: String) {
        Glide.with(requireContext())
            .load(personalPhotoUrl)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .into(binding.photoImageView)
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

    private fun isValidDate(): Boolean {
        // TO CHANGE THE FIRST CONDITION LATER //
        logD(
            binding.emailEt.text.toString() + "\n " +
                    binding.nameEt.text.toString() + "\n " +
                    binding.idEt.text.toString() + "\n " +
            binding.genderAutoComplete.text.toString() + "\n " +
            binding.nationalityAutoComplete.text.toString()
        )
            return  (selectedImageUri != null) &&
            (binding.emailEt.toString().isNotEmpty())
                    && (binding.nameEt.text.toString().length > 5)
                    && (binding.idEt.text.toString().length == 10)
                    && (binding.genderAutoComplete.text.toString().isNotEmpty())
                    && (binding.nationalityAutoComplete.text.toString().isNotEmpty())

    }

    private fun String.isValidEmail(): Boolean {
        val emailRegex = Regex("^\\w+([.-]?\\w+)*@\\w+([.-]?\\w+)*(\\.\\w{2,3})+\$")
        return emailRegex.matches(this)
    }
    private fun fillFields(driver: Driver) {
        binding.apply {
            nameEt.setText(driver.name)
            emailEt.setText(driver.email)
            idEt.setText(driver.identityNumber)
            genderAutoComplete.setText(
                if (driver.sex == "none") {
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
//        val intent = Intent(Intent.ACTION_GET_CONTENT)
//        intent.type = "image/*"
//        startActivityForResult(intent, PICK_IMAGE_REQUEST)

        ImagePicker.with(this)
            .crop()
            .galleryOnly()
            .compress(1024)			//Final image size will be less than 1 MB(Optional)
            .maxResultSize(1080, 1080)	//Final image resolution will be less than 1080 x 1080(Optional)
            .start()
        
    }

    private fun cropImage(uri: Uri) {
        val destinationUri = Uri.fromFile(File(requireContext().cacheDir, "cropped_image.jpg"))

        UCrop.of(uri, destinationUri)
            .withAspectRatio(16f, 9f)
            .start(requireContext(), this, CROP_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            //Image Uri will not be null for RESULT_OK
            val uri: Uri = data?.data!!

            // Use Uri object instead of File to avoid storage permissions
            selectedImageUri = uri
            binding.photoImageView.setImageURI(uri)
        } else if (resultCode == ImagePicker.RESULT_ERROR) {
           toast(ImagePicker.RESULT_ERROR.toString())
        } else {
            toast("Task canceled")
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        preferences = requireActivity().getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        if (isPassed(preferences, RegistrationStatus.INFO.value)) {
            val action = DriverInfoFragmentDirections.actionDriverInfoFragmentToDriverLicenseFragment()
            findNavController().navigate(action)
        }
    }

}