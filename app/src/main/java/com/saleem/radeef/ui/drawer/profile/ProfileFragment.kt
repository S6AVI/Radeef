package com.saleem.radeef.ui.drawer.profile

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.saleem.radeef.R

import com.saleem.radeef.databinding.FragmentProfileBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProfileFragment: Fragment(R.layout.fragment_profile) {
    lateinit var binding: FragmentProfileBinding
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentProfileBinding.bind(view)
    }
}