package com.saleem.radeef.driver.ui.rides

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.saleem.radeef.R
import com.saleem.radeef.databinding.FragmentRidesBinding
import com.saleem.radeef.util.UiState
import com.saleem.radeef.util.exhaustive
import com.saleem.radeef.util.hide
import com.saleem.radeef.util.logD
import com.saleem.radeef.util.show
import com.saleem.radeef.util.toast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DriverRidesFragment : Fragment(R.layout.fragment_rides) {
    lateinit var binding: FragmentRidesBinding
    private val viewModel: DriverRideViewModel by viewModels()
    private val adapter by lazy {
        DriverRideAdapter(requireContext())
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentRidesBinding.bind(view)

        binding.backBtn.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.recyclerView.adapter = adapter

        viewModel.getRides()

        viewModel.rides.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    binding.progressBar.show()
                    true
                }

                is UiState.Failure -> {
                    binding.progressBar.hide()
                    logD(state.error.toString())
                    toast(state.error.toString())
                    true
                }

                is UiState.Success -> {
                    binding.progressBar.hide()
                    adapter.updateList(state.data.toMutableList())
                    true
                }

            }.exhaustive
        }
    }
}