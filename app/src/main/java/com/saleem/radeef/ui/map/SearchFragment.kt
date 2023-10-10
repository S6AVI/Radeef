package com.saleem.radeef.ui.map

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.LocationBias
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse
import com.google.android.libraries.places.api.net.PlacesClient
import com.saleem.radeef.R
import com.saleem.radeef.databinding.FragmentSearchBinding
import dagger.hilt.android.AndroidEntryPoint
import java.lang.Math.cos

val TAG = "savii"

@AndroidEntryPoint
class SearchFragment : Fragment(R.layout.fragment_search), SearchResultAdapter.OnItemClickListener {
    lateinit var binding: FragmentSearchBinding
    private lateinit var placesClient: PlacesClient
    private lateinit var currentLocation: LatLng
    private lateinit var token: AutocompleteSessionToken
    private lateinit var handler: Handler
    private lateinit var runnable: Runnable
    private val DELAY = 1000L
    val viewModel: MapViewModel by activityViewModels()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentSearchBinding.bind(view)



        currentLocation = requireArguments().getParcelable("current")!!

        Places.initialize(requireContext(), getString(R.string.google_maps_key))

        placesClient = Places.createClient(requireContext())

        // Create a new token for the autocomplete session. Pass this to FindAutocompletePredictionsRequest,
        // and once again when the user makes a selection (for example when calling fetchPlace()).
        token = AutocompleteSessionToken.newInstance()

        // Create a RectangularBounds object.
        val bounds = RectangularBounds.newInstance(
            LatLng(-33.880490, 151.184363),
            LatLng(-33.858754, 151.229596)
        )
        // Use the builder to create a FindAutocompletePredictionsRequest.


        handler = Handler(Looper.getMainLooper())
        runnable = Runnable {

            val query = if (binding.pickupEt.hasFocus()) {
                binding.pickupEt.text.toString()
            } else {
                binding.destinationEt.text.toString()
            }
            findPredictions(query)
        }
        Log.d(TAG, handler.toString())


        binding.pickupEt.addTextChangedListener(object : TextWatcher {

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
        binding.destinationEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                handler.removeCallbacks(runnable)
                handler.postDelayed(runnable, DELAY)
                //Log.d(TAG, "postDelayed called with delay: $DELAY")
                //findPredictions(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {
                updateButton()
            }
        })


        binding.myLocation.setOnClickListener {
            binding.pickupEt.setText("Current Location")
        }

        binding.searchBtn.setOnClickListener {
            val action = SearchFragmentDirections.actionSearchFragmentToHomeFragment()
            findNavController().navigate(action)
        }
    }

    private fun updateButton() {
        binding.searchBtn.isEnabled =
            binding.pickupEt.text?.isNotEmpty()!! &&
                    binding.destinationEt.text?.isNotEmpty()!!
    }

    private fun findPredictions(query: String) {

        Log.d(TAG, "findAutocompletePredictions called with query: $query")

        val bounds = getBounds()
        val request =
            FindAutocompletePredictionsRequest.builder()
                // Call either setLocationBias() OR setLocationRestriction().
                //.setLocationRestriction(bounds)
                //setOrigin(LatLng(-33.8749937, 151.2041382))
                .setOrigin(currentLocation)
                .setCountries("SA")
                .setLocationBias(bounds)
                .setSessionToken(token)
                //.setQuery(binding.pickupInput.text.toString())
                .setQuery(query)
                .build()

        placesClient.findAutocompletePredictions(request)
            .addOnSuccessListener { response: FindAutocompletePredictionsResponse ->
                val result = response.autocompletePredictions.filter {
                    it.distanceMeters != null
                }.sortedBy { it.distanceMeters }.take(10)
                val adapter = SearchResultAdapter(this, result)
                binding.resultsRecyclerView.adapter = adapter

                for (prediction in response.autocompletePredictions) {
                    //if (prediction.distanceMeters == null)
                    Log.i(TAG, prediction.getFullText(null).toString())
                    val distance = prediction.distanceMeters?.div(1000.0) ?: 0.0
                    Log.i(TAG, String.format("%.1f km", distance))
                    //Log.i(TAG, prediction.getPrimaryText(null).toString())
                }
            }.addOnFailureListener { exception: Exception? ->
                if (exception is ApiException) {
                    Log.e(TAG, "Place not found: ${exception.statusCode}")
                }
            }
    }

    private fun getBounds(): LocationBias {

        val radiusInMeters = 100000 // 100 km in meters
        val bounds = RectangularBounds.newInstance(
            LatLng(
                currentLocation.latitude - 1 / 111.0 * radiusInMeters,
                currentLocation.longitude - 1 / (111.0 * cos(currentLocation.latitude)) * radiusInMeters
            ),
            LatLng(
                currentLocation.latitude + 1 / 111.0 * radiusInMeters,
                currentLocation.longitude + 1 / (111.0 * cos(currentLocation.latitude)) * radiusInMeters
            )
        )

        return bounds
    }

    override fun onItemClick(item: AutocompletePrediction) {
        val address = item.getFullText(null)
        if (binding.pickupEt.hasFocus()) {
            binding.pickupEt.setText(address)
            viewModel.pickup = binding.pickupEt.text.toString()
            Log.d(TAG, "in viewmodel ${viewModel.pickup.toString()}")
        } else if (binding.destinationEt.hasFocus()) {
            binding.destinationEt.setText(item.getFullText(null))
            viewModel.destination = address.toString()
        }
    }
}