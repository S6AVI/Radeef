package com.saleem.radeef.ui.drawer.wallet

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.saleem.radeef.R
import com.saleem.radeef.databinding.FragmentPaymentBinding

import com.saleem.radeef.databinding.FragmentProfileBinding
import com.saleem.radeef.databinding.FragmentSettingsBinding
import com.saleem.radeef.databinding.FragmentWalletBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WalletFragment: Fragment(R.layout.fragment_wallet) {
    lateinit var binding: FragmentWalletBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentWalletBinding.bind(view)

        binding.backButton.setOnClickListener{
            findNavController().popBackStack()
        }
    }
}