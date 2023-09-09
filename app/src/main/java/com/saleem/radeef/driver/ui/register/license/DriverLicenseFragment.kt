package com.saleem.radeef.driver.ui.register.license

import android.app.Activity
import android.app.DatePickerDialog
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
import com.saleem.radeef.data.firestore.driver.License
import com.saleem.radeef.data.firestore.driver.RegistrationStatus
import com.saleem.radeef.databinding.DriverLicenseFragmentBinding
import com.saleem.radeef.driver.ui.register.info.DriverInfoFragmentDirections
import com.saleem.radeef.util.ImageFileNames
import com.saleem.radeef.util.UiState
import com.saleem.radeef.util.bloodTypes
import com.saleem.radeef.util.disable
import com.saleem.radeef.util.enable
import com.saleem.radeef.util.hide
import com.saleem.radeef.util.isPassed
import com.saleem.radeef.util.logD
import com.saleem.radeef.util.show
import com.saleem.radeef.util.toast
import com.saleem.radeef.util.updateRegistrationStatus
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class DriverLicenseFragment() : Fragment(R.layout.driver_license_fragment) {
    private lateinit var binding: DriverLicenseFragmentBinding
    val viewModel: DriverLicenseViewModel by viewModels()
    private lateinit var preferences: SharedPreferences
    private var selectedImageUri: Uri? = null

    private var selectedIssueDate: Date? = null
    private var selectedExpDate: Date? = null

    val calendar = Calendar.getInstance()
    val currentYear = calendar.get(Calendar.YEAR)
    val currentMonth = calendar.get(Calendar.MONTH)
    val currentDay = calendar.get(Calendar.DAY_OF_MONTH)


    private val datePickerListener =
        DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            // Handle the selected date here
            val selectedDate = Calendar.getInstance()
            selectedDate.set(year, month, dayOfMonth)

            // Convert the selectedDate to your desired date format, e.g., using SimpleDateFormat
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val formattedDate = dateFormat.format(selectedDate.time)

            // Set the formatted date to your TextInputEditText or any other view as needed
            binding.issEt.setText(formattedDate)
        }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = DriverLicenseFragmentBinding.bind(view)



        setAdapters()

        binding.cameraIcon.setOnClickListener {
            openImagePicker()
            toast("icon clicked")
        }

        binding.continueBt.setOnClickListener {
            logD("isValidData: ${isValidData()}")
            if (isValidData()) {
                logD("passed isValidData()")
                binding.continueBt.disable()
                viewModel.onContinueClicked(selectedImageUri!!, ImageFileNames.LICENSE)
            } else {
                toast("Missing required data")
            }
        }

        binding.issDateInputLayout.setEndIconOnClickListener {
            // Create and show the DatePickerDialog
            showDatePickerDialog(selectedIssueDate) { date ->
                selectedIssueDate = date
                val formattedDate = formatDate(date) // Format the date as needed
                binding.issEt.setText(formattedDate)
            }
        }

        binding.expDateInputLayout.setEndIconOnClickListener {
            showDatePickerDialog(selectedExpDate) { date ->
                selectedExpDate = date
                val formattedDate = formatDate(date) // Format the date as needed
                binding.expEt.setText(formattedDate)
            }
        }

        viewModel.license.observe(viewLifecycleOwner) { state ->
            when (state) {
                UiState.Loading -> {
                    binding.baseLayout.hide()
                    binding.waitProgressBar.show()
                }

                is UiState.Success -> {
                    logD("${state.data}")
                    loadImage(state.data.photoUrl)
                    binding.baseLayout.show()
                    binding.waitProgressBar.hide()
                    fillFields(state.data)
                }

                is UiState.Failure -> {
                    binding.baseLayout.show()
                    binding.waitProgressBar.hide()
                    toast(state.error.toString())
                }

            }
        }

        viewModel.uploadImage.observe(viewLifecycleOwner) { state ->
            when (state) {
                UiState.Loading -> {
                    binding.progressBar.show()
                    binding.continueBt.setText("")
                }

                is UiState.Success -> {
                    binding.progressBar.hide()
                    logD("LicenseFragment: uploadImage: Success: ${state.data}")
                    //logD("createLicense: ${createLicense(state.data)}")
                    val license = createLicense(state.data)
                    logD("create license: ${license.toString()}")
                    viewModel.updateLicenseInfo(license = license)
                    //logD(state.data.toString())

                }

                is UiState.Failure -> {
                    binding.progressBar.hide()
                    binding.continueBt.enable()
                    binding.continueBt.setText(getString(R.string.continue_label))
                    toast(state.error.toString())
                }

            }
        }

        viewModel.updateLicense.observe(viewLifecycleOwner) {state ->
            when (state) {
                UiState.Loading -> {
                    binding.progressBar.show()
                }
                is UiState.Success -> {
                    binding.progressBar.hide()
                    logD(state.data)
                    updateRegistrationStatus(RegistrationStatus.VEHICLE, requireActivity())
                    val action = DriverLicenseFragmentDirections.actionDriverLicenseFragmentToDriverVehicleFragment()
                    findNavController().navigate(action)
                }
                is UiState.Failure -> {
                    binding.progressBar.hide()
                    toast(state.error.toString())
                }

            }
        }

    }

    private fun createLicense(uri: Uri): License {
        logD("LicenseFragment: createLicense: viewModel.LicenseData: ${viewModel.licenseData}")
        val licenseTemp = viewModel.licenseData!!
        val licenseWithFields =
        licenseTemp.copy(
            photoUrl = uri.toString(),
            issDate = selectedIssueDate!!,
            expDate = selectedExpDate!!,
            bloodType = binding.bloodAutoComplete.text.toString()
        )
        logD("LicenseFragment: createLicense: licenseWithFields: $licenseWithFields")
        return licenseWithFields
    }

    private fun setAdapters() {
        //toast("in set adapters")
        val bloodAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, bloodTypes)
        binding.bloodAutoComplete.setAdapter(bloodAdapter)
    }


    private fun isValidData(): Boolean {
        return (selectedImageUri != null && selectedImageUri.toString().isNotEmpty()) &&
                (binding.issEt.text.toString().isNotEmpty()) &&
                (binding.expEt.text.toString().isNotEmpty()) &&
                (isValidDateRange(binding.issEt.text.toString(),binding.expEt.text.toString()))

    }


    private fun fillFields(license: License) {
        binding.apply {
            issEt.setText(license.formattedIssDate.toString())
            expEt.setText(license.formattedExpDate.toString())
            bloodAutoComplete.setText(license.bloodType)
            setAdapters()
            selectedImageUri = license.photoUrl.toUri()
            logD("selectedImageUri: $selectedImageUri")
            binding.waitProgressBar.hide()
            selectedIssueDate = license.issDate
            selectedExpDate = license.expDate
        }

    }

    private fun openImagePicker() {
        ImagePicker.with(this)
            .crop(16f, 9f)
            .galleryOnly()
            .compress(1024)            //Final image size will be less than 1 MB(Optional)
            .maxResultSize(
                800,
                600
            )    //Final image resolution will be less than 1080 x 1080(Optional)
            .start()

    }

    private fun loadImage(personalPhotoUrl: String) {
        Glide.with(requireContext())
            .load(personalPhotoUrl)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
            .into(binding.photoImageView)
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

    private fun showDatePickerDialog(selectedDate: Date?, onDateSet: (Date) -> Unit) {
        val calendar = Calendar.getInstance()
        if (selectedDate != null) {
            calendar.time = selectedDate
        }

        val datePickerListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            calendar.set(year, month, dayOfMonth)
            val selectedDate = calendar.time
            onDateSet(selectedDate)
        }

        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            datePickerListener,
            currentYear,
            currentMonth,
            currentDay
        )
        datePickerDialog.show()
    }

    private fun formatDate(date: Date): String {
        // Format the date using SimpleDateFormat or any other date formatting method
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date)
    }

    private fun isValidDateRange(issDateStr: String, expDateStr: String): Boolean {
        val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val issDate = dateFormatter.parse(issDateStr)!!
        val expDate = dateFormatter.parse(expDateStr)!!

        return expDate.after(issDate)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        preferences = requireActivity().getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        if (isPassed(preferences, RegistrationStatus.LICENSE.value)) {
            val action =
                DriverLicenseFragmentDirections.actionDriverLicenseFragmentToDriverVehicleFragment()
            findNavController().navigate(action)
        }
        logD("leaving onAttach")
    }

}