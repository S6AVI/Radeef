package com.saleem.radeef.ui.entername

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.saleem.radeef.R
import com.saleem.radeef.databinding.FragmentEnterNameBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EnterNameFragment(): Fragment(R.layout.fragment_enter_name) {
    private lateinit var binding: FragmentEnterNameBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentEnterNameBinding.bind(view)
    }
}