package com.saleem.radeef.ui.drawer.wallet

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.braintreepayments.cardform.view.CardForm
import com.saleem.radeef.R
import com.saleem.radeef.databinding.FragmentAddMethodBinding
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class AddMethodFragment: Fragment(R.layout.fragment_add_method) {
    lateinit var binding: FragmentAddMethodBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentAddMethodBinding.bind(view)

//        binding.backButton.setOnClickListener{
//            findNavController().popBackStack()
//        }

        val cardForm = binding.cardForm

        cardForm.cardRequired(true)
            .expirationRequired(true)
            .cvvRequired(true)
            .cardholderName(CardForm.FIELD_REQUIRED)
            .postalCodeRequired(false)
            .mobileNumberRequired(false)
            .actionLabel("Purchase")
            .setup(requireActivity())

        binding.backBtn.setOnClickListener{
            findNavController().popBackStack()
        }
    }
}