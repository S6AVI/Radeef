package com.saleem.radeef.ui.drawer.payment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.saleem.radeef.R
import com.saleem.radeef.databinding.FragmentPaymentBinding

import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DriverPaymentFragment: Fragment(R.layout.fragment_payment) {
    lateinit var binding: FragmentPaymentBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentPaymentBinding.bind(view)

        binding.backBtn.setOnClickListener{
            findNavController().popBackStack()
        }

        binding.addButton.setOnClickListener {
            val action = DriverPaymentFragmentDirections.actionDriverPaymentFragmentToDriverAddMethodFragment()
            findNavController().navigate(action)
        }
    }
}