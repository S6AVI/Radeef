package com.saleem.radeef.driver.ui.register.vehicle

import android.app.Activity
import android.content.Context
import android.content.Intent
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
//import com.github.drjacky.imagepicker.ImagePicker
import com.saleem.radeef.R
import com.saleem.radeef.data.firestore.driver.Driver
import com.saleem.radeef.data.firestore.driver.RegistrationStatus
import com.saleem.radeef.data.firestore.driver.Vehicle
import com.saleem.radeef.databinding.DriverVehicleFragmentBinding
import com.saleem.radeef.driver.ui.register.license.DriverLicenseFragmentDirections
import com.saleem.radeef.util.ImageFileNames
import com.saleem.radeef.util.UiState
import com.saleem.radeef.util.carCapacities
import com.saleem.radeef.util.carColors
import com.saleem.radeef.util.disable
import com.saleem.radeef.util.enable
import com.saleem.radeef.util.getYears
import com.saleem.radeef.util.hide
import com.saleem.radeef.util.isValidPlateNumber
import com.saleem.radeef.util.logD
import com.saleem.radeef.util.show
import com.saleem.radeef.util.toast
import com.saleem.radeef.util.updateRegistrationStatus
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DriverVehicleFragment() : Fragment(R.layout.driver_vehicle_fragment) {
    private lateinit var binding: DriverVehicleFragmentBinding
    val viewModel: DriverVehicleViewModel by viewModels()
    private var selectedImageUri: Uri? = null


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = DriverVehicleFragmentBinding.bind(view)

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
                viewModel.onContinueClicked(selectedImageUri!!, ImageFileNames.VEHICLE)
            } else {
                toast("Missing required data")
            }
        }




        viewModel.vehicle.observe(viewLifecycleOwner) { state ->
            when (state) {
                UiState.Loading -> {
                    binding.baseLayout.hide()
                    binding.waitProgressBar.show()
                }

                is UiState.Success -> {
                    logD("${state.data}")
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
            //val filtered = it.filter { it.startsWith("") }
            val makesAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, it.sorted())
            binding.makeAutoComplete.setAdapter(makesAdapter)
            logD("done!")
            logD(it.toString())
        }

        binding.makeAutoComplete.setOnItemClickListener { parent, _, position, _ ->
            val item = parent.getItemAtPosition(position).toString()
            logD(item)
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
                    logD(state.data.toString())

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
                    binding.continueBt.setText("")
                }

                is UiState.Success -> {
                    binding.progressBar.hide()
                    logD("VehicleFragment: uploadImage: Success: ${state.data}")
                    //logD("createvehicle: ${createvehicle(state.data)}")
                    val vehicle = createVehicle(state.data)
                    logD("create vehicle: ${vehicle.toString()}")
                    viewModel.updateVehicleInfo(vehicle = vehicle)
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

        viewModel.updateVehicle.observe(viewLifecycleOwner) {state ->
            when (state) {
                UiState.Loading -> {
                    binding.progressBar.show()
                }
                is UiState.Success -> {
                    binding.progressBar.hide()
                    logD(state.data)
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
        logD("VehicleFragment: createVehicle: viewModel.VehicleData: ${viewModel.vehicleData}")
        val vehicleTemp = viewModel.vehicleData!!
        val vehicleWithFields =
            vehicleTemp.copy(
                photoUrl = uri.toString(),
                plateNumber = binding.plateEt.text.toString(),
                make = binding.makeAutoComplete.text.toString(),
                model = binding.modelAutoComplete.text.toString(),
                color = binding.colorAutoComplete.text.toString(),
                year = binding.yearAutoComplete.text.toString().toInt(),
                numberOfSeats = binding.capacityAutoComplete.text.toString().toInt()

            )
        logD("VehicleFragment: createVehicle: vehicleWithFields: $vehicleWithFields")
        return vehicleWithFields
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
            yearAutoComplete.setText(vehicle.year.toString())
            capacityAutoComplete.setText(vehicle.numberOfSeats.toString())
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

    private fun isValidData(): Boolean {
        return (selectedImageUri != null && selectedImageUri.toString().isNotEmpty()) &&
                (binding.makeAutoComplete.text.toString().isNotEmpty()) &&
                (binding.modelAutoComplete.text.toString().isNotEmpty()) &&
                (binding.colorAutoComplete.text.toString().isNotEmpty()) &&
                (binding.yearAutoComplete.text.toString().isNotEmpty()) &&
                (binding.capacityAutoComplete.text.toString().isNotEmpty()) &&
                (binding.plateEt.text.toString().isValidPlateNumber())


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
//        preferences = requireActivity().getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
//        if (isPassed(preferences, RegistrationStatus.vehicle.value)) {
//            val action =
//                DrivervehicleFragmentDirections.actionDrivervehicleFragmentToDriverVehicleFragment()
//            findNavController().navigate(action)
//        }
//        logD("leaving onAttach")
    }

}


