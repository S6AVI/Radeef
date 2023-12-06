package com.saleem.radeef.driver.ui.home

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.LocationBias
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse
import com.google.android.libraries.places.api.net.PlacesClient
import com.saleem.radeef.R
import com.saleem.radeef.data.model.RadeefLocation
import com.saleem.radeef.databinding.FragmentSearchBinding
import com.saleem.radeef.util.DELAY
import com.saleem.radeef.util.SearchResultAdapter
import com.saleem.radeef.util.TAG
import com.saleem.radeef.util.UiState
import com.saleem.radeef.util.disable
import com.saleem.radeef.util.hide
import com.saleem.radeef.util.logD
import com.saleem.radeef.util.toast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlin.math.cos


@AndroidEntryPoint
class DriverSearchFragment : Fragment(R.layout.fragment_search),
    SearchResultAdapter.OnItemClickListener {
    lateinit var binding: FragmentSearchBinding
    private lateinit var placesClient: PlacesClient
    private lateinit var token: AutocompleteSessionToken
    private lateinit var handler: Handler
    private lateinit var runnable: Runnable
    val viewModel: DriverHomeViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentSearchBinding.bind(view)

        Places.initialize(requireContext(), getString(R.string.google_maps_key))

        placesClient = Places.createClient(requireContext())

        token = AutocompleteSessionToken.newInstance()

        handler = Handler(Looper.getMainLooper())
        runnable = Runnable {

            val query = if (binding.pickupEt.hasFocus()) {
                binding.pickupEt.text.toString()
            } else {
                binding.destinationEt.text.toString()
            }
            findPredictions(query)
        }


        binding.searchBtn.setOnClickListener {

            viewModel.updateDriverLocations()

        }

        binding.backBtn.setOnClickListener {
            findNavController().popBackStack()
        }
        observer()
    }

    private fun updateButton() {
        binding.searchBtn.isEnabled =
            binding.pickupEt.text?.isNotEmpty()!! &&
                    binding.destinationEt.text?.isNotEmpty()!!
    }

    private fun findPredictions(query: String) {

        val bounds = getBounds()
        val request =
            FindAutocompletePredictionsRequest.builder()
                .setOrigin(viewModel.driverData?.pickupLatLng)
                .setCountries("SA")
                .setLocationBias(bounds)
                .setSessionToken(token)
                .setQuery(query)
                .build()

        placesClient.findAutocompletePredictions(request)
            .addOnSuccessListener { response: FindAutocompletePredictionsResponse ->
                val result = response.autocompletePredictions.filter {
                    it.distanceMeters != null
                }.sortedBy { it.distanceMeters }.take(10)
                val adapter = SearchResultAdapter(this, result)
                binding.resultsRecyclerView.adapter = adapter

            }.addOnFailureListener { exception: Exception? ->
                if (exception is ApiException) {
                    Log.e(TAG, "Place not found: ${exception.statusCode}")
                }
            }
    }

    private fun getBounds(): LocationBias {

        val radiusInMeters = 100000 // 100 km in meters
        val currentLocation = viewModel.driverData!!.pickupLatLng

        return RectangularBounds.newInstance(
            LatLng(
                currentLocation.latitude - 1 / 111.0 * radiusInMeters,
                currentLocation.longitude - 1 / (111.0 * cos(currentLocation.latitude)) * radiusInMeters
            ),
            LatLng(
                currentLocation.latitude + 1 / 111.0 * radiusInMeters,
                currentLocation.longitude + 1 / (111.0 * cos(currentLocation.latitude)) * radiusInMeters
            )
        )
    }

    override fun onItemClick(item: AutocompletePrediction) {
        val address = item.getFullText(null)
        binding.destinationEt.setText(address)
        val placeId = item.placeId
        fetchDestinationLatLng(placeId, address.toString())
    }

    private fun fetchDestinationLatLng(placeId: String, address: String) {
        viewModel.viewModelScope.launch {
            val destinationLatLng = getLatLngFromPlaceId(placeId)
            if (destinationLatLng != null) {
                viewModel.destination = RadeefLocation(destinationLatLng, address)
            }
        }
    }

    private suspend fun getLatLngFromPlaceId(placeId: String): LatLng? {
        try {
        val placeFields = listOf(Place.Field.LAT_LNG)

        // Build the fetch place request
        val request = FetchPlaceRequest.newInstance(placeId, placeFields)

        // Fetch the place details
        val placeResponse = placesClient.fetchPlace(request).await()

        // Retrieve the LatLng from the place response
        val place = placeResponse.place
        return place.latLng
        } catch (e: Exception) {
            logD("error occurred in getLatLngFromPlaceId: ${e.message}")
        }
        return LatLng(.0,.0)
    }

    private fun observer() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.homeEvent.collect { event ->
                when (event) {
                    is DriverHomeViewModel.HomeEvent.UpdateResult -> {
                        when (event.state) {
                            UiState.Loading -> {
                            }

                            is UiState.Success -> {
                                if (event.state.data) {
                                    findNavController().popBackStack()
                                }
                            }

                            is UiState.Failure -> {
                                logD("DriverSearchFragment - 148: failure: ${event.state.error}")
                                toast(event.state.error)
                            }
                        }
                    }

                    else -> {}
                }

            }
        }

        binding.destinationEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                handler.removeCallbacks(runnable)
                handler.postDelayed(runnable, DELAY)
            }

            override fun afterTextChanged(s: Editable?) {
                updateButton()
            }
        })
        binding.myLocation.hide()
        binding.pickupIl.disable()
    }

}