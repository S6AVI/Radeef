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
import com.saleem.radeef.R
import com.saleem.radeef.data.model.License
import com.saleem.radeef.util.RegistrationStatus
import com.saleem.radeef.databinding.DriverLicenseFragmentBinding
import com.saleem.radeef.util.ImageFileNames
import com.saleem.radeef.util.UiState
import com.saleem.radeef.util.bloodTypes
import com.saleem.radeef.util.disable
import com.saleem.radeef.util.enable
import com.saleem.radeef.util.hide
import com.saleem.radeef.util.isPassed
import com.saleem.radeef.util.logD
import com.saleem.radeef.util.parseDate
import com.saleem.radeef.util.show
import com.saleem.radeef.util.toast
import com.saleem.radeef.util.updateRegistrationStatus
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class DriverLicenseFragment : Fragment(R.layout.driver_license_fragment) {

    private lateinit var binding: DriverLicenseFragmentBinding
    val viewModel: DriverLicenseViewModel by viewModels()
    private lateinit var preferences: SharedPreferences
    private var selectedImageUri: Uri? = null

    private var selectedIssueDate: Date? = null
    private var selectedExpDate: Date? = null



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = DriverLicenseFragmentBinding.bind(view)



        setAdapters()

        binding.cameraIcon.setOnClickListener {
            openImagePicker()

        }

        binding.continueBt.setOnClickListener {

            hideErrors()
            if (isValidData()) {
                viewModel.onContinueClicked(selectedImageUri!!, ImageFileNames.LICENSE)
                hideErrors()
            }
        }

        binding.issDateInputLayout.setEndIconOnClickListener {
            val startDate = Calendar.getInstance()
            startDate.add(Calendar.YEAR, -10)
            showDatePickerDialog(selectedIssueDate, startDate) { date ->
                selectedIssueDate = date
                val formattedDate = formatDate(date) // Format the date as needed
                binding.issEt.setText(formattedDate)
            }
        }

        binding.issDateInputLayout.setErrorIconOnClickListener {

            val startDate = Calendar.getInstance()
            startDate.add(Calendar.YEAR, -10)
            showDatePickerDialog(selectedIssueDate, startDate) { date ->
                selectedIssueDate = date
                val formattedDate = formatDate(date) // Format the date as needed
                binding.issEt.setText(formattedDate)
            }
        }

        binding.expDateInputLayout.setEndIconOnClickListener {
            val startDate = Calendar.getInstance()
            startDate.add(Calendar.YEAR, -7)
            showDatePickerDialog(selectedExpDate, startDate) { date ->
                selectedExpDate = date
                val formattedDate = formatDate(date) // Format the date as needed
                binding.expEt.setText(formattedDate)
            }
        }

        binding.expDateInputLayout.setErrorIconOnClickListener {
            val startDate = Calendar.getInstance()
            startDate.add(Calendar.YEAR, +1)
            showDatePickerDialog(selectedExpDate, startDate) { date ->
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
                    binding.continueBt.text = ""
                }

                is UiState.Success -> {
                    binding.progressBar.hide()

                    val license = createLicense(state.data)
                    viewModel.updateLicenseInfo(license = license)
                }

                is UiState.Failure -> {
                    binding.progressBar.hide()
                    binding.continueBt.enable()
                    binding.continueBt.text = getString(R.string.continue_label)
                    toast(state.error.toString())
                }

            }
        }

        viewModel.updateLicense.observe(viewLifecycleOwner) { state ->
            when (state) {
                UiState.Loading -> {
                    binding.progressBar.show()
                    binding.continueBt.disable()
                }

                is UiState.Success -> {
                    binding.progressBar.hide()
                    logD(state.data)
                    updateRegistrationStatus(RegistrationStatus.VEHICLE, requireActivity())
                    val action =
                        DriverLicenseFragmentDirections.actionDriverLicenseFragmentToDriverVehicleFragment()
                    findNavController().navigate(action)
                }

                is UiState.Failure -> {
                    binding.progressBar.hide()
                    binding.continueBt.enable()
                    logD(state.error.toString())
                }

            }
        }

    }

    private fun hideErrors() {
        binding.issDateInputLayout.isErrorEnabled = false
        binding.expDateInputLayout.isErrorEnabled = false
        binding.bloodTypeInputLayout.isErrorEnabled = false
        binding.uploadTv.setTextColor(resources.getColor(R.color.black))
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
        var isValid = true

        if (selectedImageUri == null || selectedImageUri.toString().isEmpty()) {
            binding.uploadTv.setTextColor(resources.getColor(R.color.md_theme_light_error))
            isValid = false
        }
        if (binding.issEt.text.toString().isEmpty()) {
            binding.issDateInputLayout.error = getString(R.string.issue_date_is_required)
            isValid = false
            //binding.issDateInputLayout.isErrorEnabled = false
        } else {
            val issueDate = binding.issEt.text.toString().parseDate()
            val currentDate = Date()

            if (issueDate != null && issueDate.after(currentDate)) {
                binding.issDateInputLayout.error = getString(R.string.issue_date_cannot_be_future)
                isValid = false
                binding.issDateInputLayout.isErrorEnabled = false
            }
        }

        if (binding.expEt.text.toString().isEmpty()) {
            binding.expDateInputLayout.error = getString(R.string.expiration_date_is_required)
            isValid = false
        } else {
            val expirationDate =
                binding.expEt.text.toString().parseDate() // Parse the expiration date string to Date object
            val currentDate = Calendar.getInstance().time // Get the current date

            if (expirationDate != null && expirationDate.before(currentDate)) {
                binding.expDateInputLayout.error =
                    getString(R.string.expiration_date_cannot_be_past)
                isValid = false
            }
        }

        if (binding.issEt.text.toString().isNotEmpty() && binding.expEt.text.toString().isNotEmpty()) {
            if (!isValidDateRange(binding.issEt.text.toString(), binding.expEt.text.toString())) {
                binding.issDateInputLayout.error = getString(R.string.error_date_range)
                binding.expDateInputLayout.error = getString(R.string.error_date_range)
                isValid = false
            }
        }

        if (binding.bloodAutoComplete.text.toString().isEmpty()) {
            binding.bloodTypeInputLayout.error = getString(R.string.blood_type_should_be_specified)
        }

        return isValid
    }


    private fun fillFields(license: License) {
        binding.apply {
            issEt.setText(license.formattedIssDate)
            expEt.setText(license.formattedExpDate)
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
            .compress(1024)
            .maxResultSize(
                800,
                600
            )
            .start()
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


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (resultCode) {
            Activity.RESULT_OK -> {
                //Image Uri will not be null for RESULT_OK
                val uri: Uri = data?.data!!

                // Use Uri object instead of File to avoid storage permissions
                selectedImageUri = uri
                binding.photoImageView.setImageURI(uri)
            }
            ImagePicker.RESULT_ERROR -> {
                toast(ImagePicker.RESULT_ERROR.toString())
            }
            else -> {
            }
        }
    }

    private fun showDatePickerDialog(
        selectedDate: Date?,
        start: Calendar,
        onDateSet: (Date) -> Unit
    ) {
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

        datePickerDialog.datePicker.minDate =
            start.timeInMillis // Set the minimum date as the start date
        datePickerDialog.show()
    }

    private fun formatDate(date: Date): String {
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
    }

}