package com.saleem.radeef.ui.drawer.help

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.saleem.radeef.R
import com.saleem.radeef.databinding.DriverFragmentHelpBinding
import com.saleem.radeef.databinding.FragmentHelpBinding

import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DriverHelpFragment: Fragment(R.layout.driver_fragment_help) {
    lateinit var binding: DriverFragmentHelpBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = DriverFragmentHelpBinding.bind(view)

        binding.base.backBtn.setOnClickListener{
            findNavController().popBackStack()
        }
    }
}