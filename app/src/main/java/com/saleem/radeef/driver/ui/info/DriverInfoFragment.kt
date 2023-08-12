package com.saleem.radeef.driver.ui.info

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.saleem.radeef.R
import com.saleem.radeef.databinding.DriverInfoFragmentBinding
import com.saleem.radeef.util.genders
import com.saleem.radeef.util.getCountries
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DriverInfoFragment() : Fragment(R.layout.driver_info_fragment) {
    private lateinit var binding: DriverInfoFragmentBinding
    val viewModel: DriverInfoViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = DriverInfoFragmentBinding.bind(view)


        val nationalityAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, getCountries())
        binding.nationalityAutoComplete.setAdapter(nationalityAdapter)


        val genderAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, genders)
        binding.genderAutoComplete.setAdapter(genderAdapter)

    }


}