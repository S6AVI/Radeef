package com.saleem.radeef.passenger.ui.help

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.saleem.radeef.R
import com.saleem.radeef.databinding.FragmentHelpBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HelpFragment: Fragment(R.layout.fragment_help) {
    lateinit var binding: FragmentHelpBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentHelpBinding.bind(view)

        binding.backBtn.setOnClickListener{
            findNavController().popBackStack()
        }
    }
}