package com.saleem.radeef.auth

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.saleem.radeef.R
import com.saleem.radeef.databinding.FragmentEnterNameBinding
import com.saleem.radeef.ui.enternumber.RegisterViewModel
import com.saleem.radeef.util.UiState
import com.saleem.radeef.util.exhaustive
import com.saleem.radeef.util.toast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EnterNameFragment(): Fragment(R.layout.fragment_enter_name) {
    private lateinit var binding: FragmentEnterNameBinding
    val viewModel: RegisterViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentEnterNameBinding.bind(view)

        binding.continueBt.setOnClickListener {
            val name = binding.nameEt.text?.trim().toString()
            if (name.length > 4) {
                viewModel.updateName(name)
            }
        }

        viewModel.name.observe(viewLifecycleOwner) {state ->
            when(state) {
                UiState.Loading -> {}
                is UiState.Success -> {
                    toast(state.data)
                    val action =
                        EnterNameFragmentDirections.actionEnterNameFragmentToRidesFragment()
                    findNavController().navigate(action)
                }
                is UiState.Failure -> toast(state.error)


            }.exhaustive

        }

//        val action = EnterNameFragmentDirections.actionEnterNameFragmentToRidesFragment()
//        findNavController().navigate(action)
    }
}