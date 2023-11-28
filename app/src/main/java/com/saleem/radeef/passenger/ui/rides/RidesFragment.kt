package com.saleem.radeef.passenger.ui.rides

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.saleem.radeef.R
import com.saleem.radeef.databinding.FragmentRidesBinding
import com.saleem.radeef.driver.ui.drawer.rides.DriverRideAdapter
import com.saleem.radeef.ui.rides.DriverRideViewModel
import com.saleem.radeef.util.UiState
import com.saleem.radeef.util.exhaustive
import com.saleem.radeef.util.hide
import com.saleem.radeef.util.show
import com.saleem.radeef.util.toast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RidesFragment : Fragment(R.layout.fragment_rides) {
    lateinit var binding: FragmentRidesBinding
    private val viewModel: RideViewModel by viewModels()
    private val adapter by lazy {
        RideAdapter(requireContext())
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
                    Log.d("savii", "Loading")
                    true
                }

                is UiState.Failure -> {
                    binding.progressBar.hide()
                    toast("something")
                    Log.d("savii", state.error.toString())
                    true
                }

                is UiState.Success -> {
                    Log.d("savii", "passed to here")
                    binding.progressBar.hide()
                    adapter.updateList(state.data.toMutableList())
                    Log.d("savii", state.data.size.toString())

                    state.data.forEach {
                        Log.d("savii", it.toString())
                    }
                    true
                }

            }.exhaustive
        }


    }
}