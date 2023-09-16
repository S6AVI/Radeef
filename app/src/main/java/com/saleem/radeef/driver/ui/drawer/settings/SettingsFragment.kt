package com.saleem.radeef.ui.drawer.settings

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.saleem.radeef.R
import com.saleem.radeef.databinding.FragmentPaymentBinding

import com.saleem.radeef.databinding.FragmentProfileBinding
import com.saleem.radeef.databinding.FragmentSettingsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DriverSettingsFragment: Fragment(R.layout.fragment_settings) {
    lateinit var binding: FragmentSettingsBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentSettingsBinding.bind(view)
    }
}