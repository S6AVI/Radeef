package com.saleem.radeef.driver.ui.register.vehicle

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
import com.saleem.radeef.InfoNavigationDirections
import com.saleem.radeef.R
import com.saleem.radeef.util.RegistrationStatus
import com.saleem.radeef.data.model.Vehicle
import com.saleem.radeef.databinding.DriverVehicleFragmentBinding
import com.saleem.radeef.util.ImageFileNames
import com.saleem.radeef.util.UiState
import com.saleem.radeef.util.carCapacities
import com.saleem.radeef.util.carColors
import com.saleem.radeef.util.disable
import com.saleem.radeef.util.enable
import com.saleem.radeef.util.getYears
import com.saleem.radeef.util.hide
import com.saleem.radeef.util.isPassed
import com.saleem.radeef.util.isValidPlateNumber
import com.saleem.radeef.util.logD
import com.saleem.radeef.util.show
import com.saleem.radeef.util.toast
import com.saleem.radeef.util.updateRegistrationStatus
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DriverVehicleFragment : Fragment(R.layout.driver_vehicle_fragment) {
    private lateinit var binding: DriverVehicleFragmentBinding
    val viewModel: DriverVehicleViewModel by viewModels()
    private lateinit var preferences: SharedPreferences
    private var selectedImageUri: Uri? = null


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = DriverVehicleFragmentBinding.bind(view)

        setAdapters()

        binding.cameraIcon.setOnClickListener {
            openImagePicker()
        }

        binding.continueBt.setOnClickListener {
            if (isValidData()) {

                binding.continueBt.disable()
                viewModel.onContinueClicked(selectedImageUri!!, ImageFileNames.VEHICLE)
            }
        }




        viewModel.vehicle.observe(viewLifecycleOwner) { state ->
            when (state) {
                UiState.Loading -> {
                    binding.baseLayout.hide()
                    binding.waitProgressBar.show()
                }

                is UiState.Success -> {
                    loadImage(state.data.photoUrl)
                    fillFields(state.data)
                    binding.waitProgressBar.hide()
                    binding.baseLayout.show()

                }

                is UiState.Failure -> {
                    binding.baseLayout.show()
                    binding.waitProgressBar.hide()
                    toast(state.error.toString())
                }

            }
        }

        viewModel.makesData.observe(viewLifecycleOwner) {
            val makesAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, it.sorted())
            binding.makeAutoComplete.setAdapter(makesAdapter)
        }

        binding.makeAutoComplete.setOnItemClickListener { parent, _, position, _ ->
            val item = parent.getItemAtPosition(position).toString()
            binding.modelAutoComplete.setText("")
            viewModel.fetchModels(item)
        }



        viewModel.modelsData.observe(viewLifecycleOwner) { state ->
            when (state) {
                UiState.Loading -> {
                    binding.progressBar.show()
                    binding.baseLayout.hide()
                }

                is UiState.Success -> {
                    val modelsAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, state.data.sorted())
                    binding.modelAutoComplete.setAdapter(modelsAdapter)
                    binding.progressBar.hide()
                    binding.modelInputLayout.show()
                    binding.baseLayout.show()
                }

                is UiState.Failure -> {
                    logD(state.error.toString())
                    binding.progressBar.hide()
                    binding.baseLayout.show()
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

                    val vehicle = createVehicle(state.data)
                    viewModel.updateVehicleInfo(vehicle = vehicle)

                }

                is UiState.Failure -> {
                    binding.progressBar.hide()
                    binding.continueBt.enable()
                    binding.continueBt.text = getString(R.string.continue_label)
                    toast(state.error.toString())
                }

            }
        }

        viewModel.updateVehicle.observe(viewLifecycleOwner) {state ->
            when (state) {
                UiState.Loading -> {
                    binding.progressBar.show()
                }
                is UiState.Success -> {
                    binding.progressBar.hide()
                    updateRegistrationStatus(RegistrationStatus.COMPLETED, requireActivity())
                    val action = InfoNavigationDirections.actionGlobalHomeNavigation()
                    findNavController().navigate(action)
                }
                is UiState.Failure -> {
                    binding.progressBar.hide()
                    toast(state.error.toString())
                }

            }
        }
    }

    private fun createVehicle(uri: Uri): Vehicle {
        val vehicleTemp = viewModel.vehicleData!!
        return vehicleTemp.copy(
            photoUrl = uri.toString(),
            plateNumber = binding.plateEt.text.toString(),
            make = binding.makeAutoComplete.text.toString(),
            model = binding.modelAutoComplete.text.toString(),
            color = binding.colorAutoComplete.text.toString(),
            year = binding.yearAutoComplete.text.toString().toInt(),
            numberOfSeats = binding.capacityAutoComplete.text.toString().toInt()

        )
    }


    private fun setAdapters() {
        val colorsAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, carColors)
        binding.colorAutoComplete.setAdapter(colorsAdapter)

        val yearsAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, getYears())
        binding.yearAutoComplete.setAdapter(yearsAdapter)

        val capacityAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, carCapacities)
        binding.capacityAutoComplete.setAdapter(capacityAdapter)
    }

    private fun fillFields(vehicle: Vehicle) {
        binding.apply {
            plateEt.setText(vehicle.plateNumber)
            makeAutoComplete.setText(vehicle.make)
            modelAutoComplete.setText(vehicle.model)

            colorAutoComplete.setText(vehicle.color)

            if (vehicle.year != 0) {
                yearAutoComplete.setText(vehicle.year.toString())
            }
            if (vehicle.numberOfSeats != 0) {
                capacityAutoComplete.setText(vehicle.numberOfSeats.toString())
            }

            setAdapters()
            selectedImageUri = vehicle.photoUrl.toUri()
        }

        if (vehicle.make.isNotEmpty()) {
            binding.modelInputLayout.show()
            viewModel.fetchModels(vehicle.make)
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

    private fun isValidData(): Boolean {
        var isValid = true

        if (selectedImageUri == null || selectedImageUri.toString().isEmpty()) {
            binding.uploadTv.setTextColor(resources.getColor(R.color.md_theme_light_error))
            isValid = false
        } else {
            binding.uploadTv.setTextColor(resources.getColor(R.color.black))
        }

        if (binding.makeAutoComplete.text.toString().isEmpty()) {
            binding.makeInputLayout.error = getString(R.string.make_is_required)
            isValid = false
        } else {
            binding.makeInputLayout.isErrorEnabled = false
        }

        if (binding.modelAutoComplete.text.toString().isEmpty()) {
            binding.modelInputLayout.error = getString(R.string.model_is_required)
            isValid = false
        } else {
            binding.modelInputLayout.isErrorEnabled = false
        }

        if (binding.colorAutoComplete.text.toString().isEmpty()) {
            binding.colorInputLayout.error = getString(R.string.color_is_required)
            isValid = false
        } else {
            binding.colorInputLayout.isErrorEnabled = false
        }

        if (binding.yearAutoComplete.text.toString().isEmpty()) {
            binding.yearInputLayout.error = getString(R.string.year_is_required)
            isValid = false
        } else {
            binding.yearInputLayout.isErrorEnabled = false
        }

        if (binding.capacityAutoComplete.text.toString().isEmpty()) {
            binding.capacityInputLayout.error = getString(R.string.capacity_is_required)
            isValid = false
        } else {
            binding.capacityInputLayout.isErrorEnabled = false
        }

        val plate = binding.plateEt.text.toString()
        if (plate.isEmpty() || !plate.isValidPlateNumber()) {
            binding.plateInputLayout.error = getString(R.string.invalid_plate_number)
            isValid = false
        } else {
            binding.plateInputLayout.isErrorEnabled = false
        }

        return isValid
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

    override fun onAttach(context: Context) {
        super.onAttach(context)
        preferences = requireActivity().getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
        if (isPassed(preferences, RegistrationStatus.VEHICLE.value)) {
            val action = InfoNavigationDirections.actionGlobalHomeNavigation()
            findNavController().navigate(action)
        }
    }

}


