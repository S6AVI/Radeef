package com.saleem.radeef.driver.ui.register.vehicle

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.github.dhaval2404.imagepicker.ImagePicker
//import com.github.drjacky.imagepicker.ImagePicker
import com.saleem.radeef.R
import com.saleem.radeef.data.firestore.driver.Driver
import com.saleem.radeef.data.firestore.driver.RegistrationStatus
import com.saleem.radeef.databinding.DriverInfoFragmentBinding
import com.saleem.radeef.databinding.DriverLicenseFragmentBinding
import com.saleem.radeef.util.Constants.CROP_IMAGE_REQUEST
import com.saleem.radeef.util.UiState
import com.saleem.radeef.util.genders
import com.saleem.radeef.util.getCountries
import com.saleem.radeef.util.hide
import com.saleem.radeef.util.logD
import com.saleem.radeef.util.show
import com.saleem.radeef.util.toast
import com.yalantis.ucrop.UCrop
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

@AndroidEntryPoint
class DriverVehicleFragment() : Fragment(R.layout.driver_license_fragment) {
    private lateinit var binding: DriverLicenseFragmentBinding
    val viewModel: DriverVehicleViewModel by viewModels()
    private var selectedImageUri: Uri? = null



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = DriverLicenseFragmentBinding.bind(view)


    }



    private fun isValidDate(): Boolean {
        // TO CHANGE THE FIRST CONDITION LATER //
            return  true

    }


    private fun fillFields(driver: Driver) {
        binding.apply {

        }

    }

}