package com.saleem.radeef.driver.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

import com.saleem.radeef.R
import com.saleem.radeef.databinding.DriverInfoFragmentBinding
import com.saleem.radeef.databinding.DriverPathDetailsFragmentBinding
import com.saleem.radeef.driver.ui.register.info.DriverInfoViewModel
import com.saleem.radeef.util.UiState
import com.saleem.radeef.util.exhaustive
import com.saleem.radeef.util.hide
import com.saleem.radeef.util.logD
import com.saleem.radeef.util.show
import kotlinx.coroutines.flow.collect


class DriverPathDetailsFragment: BottomSheetDialogFragment() {

    private lateinit var binding: DriverPathDetailsFragmentBinding
    val viewModel: DriverMapViewModel by activityViewModels()

    private var distance: Float = 0.0f


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DriverPathDetailsFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        distance = arguments?.getFloat("distance") ?: 0.0f
        //binding = DriverPathDetailsFragmentBinding.bind(view)

        logD("inside bottom sheet")
        binding.apply {
            pickupTitleTextView.text = viewModel.driverData?.pickup_title
            destinationTitleTextView.text = viewModel.driverData?.destination_title
            distanceTextView.text = "${distance.toString()} KM"

        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.homeEvent.collect {event ->
                when(event) {
                    is DriverMapViewModel.HomeEvent.StartSearching -> {

                        when (event.status) {
                            UiState.Loading -> {
                                binding.progressBar.show()
                                binding.contentLayout.hide()
                                logD("in startSearching collect: loading")

                            }
                            is UiState.Success -> {
                                binding.progressBar.hide()
                                binding.contentLayout.show()
                                logD("in startSearching collect: success")
                                findNavController().popBackStack()
                            }
                            is UiState.Failure -> {
                                binding.progressBar.hide()
                                binding.contentLayout.show()
                                logD("in startSearching collect: failure")
                            }

                        }.exhaustive
                    }
                    else -> {}
                }

            }
        }

        binding.searchButton.setOnClickListener {
            viewModel.onSearchButtonClicked()
        }

    }
}