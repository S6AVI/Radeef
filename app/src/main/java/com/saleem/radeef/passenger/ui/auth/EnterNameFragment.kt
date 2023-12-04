package com.saleem.radeef.passenger.ui.auth

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.saleem.radeef.PassengerInfoNavigationDirections
import com.saleem.radeef.R
import com.saleem.radeef.databinding.FragmentEnterNameBinding
import com.saleem.radeef.util.UiState
import com.saleem.radeef.util.disable
import com.saleem.radeef.util.enable
import com.saleem.radeef.util.exhaustive
import com.saleem.radeef.util.hide
import com.saleem.radeef.util.hideKeyboard
import com.saleem.radeef.util.logD
import com.saleem.radeef.util.show
import com.saleem.radeef.util.toast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EnterNameFragment : Fragment(R.layout.fragment_enter_name) {
    private lateinit var binding: FragmentEnterNameBinding
    val viewModel: RegisterViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        checkIfAlreadyHasName()
        binding = FragmentEnterNameBinding.bind(view)
        binding.root.hide()

        binding.continueBt.setOnClickListener {
            val name = binding.nameEt.text?.trim().toString()
            if (name.length >= 4) {
                hideKeyboard()
                binding.nameIl.error = ""
                binding.nameIl.isErrorEnabled = false
                viewModel.updateName(name)
            } else {
                binding.nameIl.error = getString(R.string.error_name)
            }
        }

        viewModel.name.observe(viewLifecycleOwner) { state ->
            when (state) {
                UiState.Loading -> {
                    binding.continueBt.text = ""
                    binding.continueBt.disable()
                    binding.progressBar.show()
                }

                is UiState.Success -> {
                    binding.progressBar.hide()
                    navigateToHome()
                }

                is UiState.Failure -> {
                    binding.progressBar.hide()
                    binding.continueBt.enable()
                    binding.continueBt.setText(R.string.continue_label)
                    logD(state.error.toString())
                    toast(state.error.toString())
                }
            }.exhaustive
        }
    }

    private fun checkIfAlreadyHasName() {
        viewModel.alreadyHasName { result ->
            if (result) {
                navigateToHome()
            } else {
                binding.root.show()
            }
        }
    }

    private fun navigateToHome() {
        val action = PassengerInfoNavigationDirections.actionGlobalPassengerHomeNavigation()
        findNavController().navigate(action)
    }

}