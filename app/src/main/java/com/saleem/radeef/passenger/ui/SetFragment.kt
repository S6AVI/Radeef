package com.saleem.radeef.passenger.ui

import android.os.Bundle
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.saleem.radeef.R
import com.saleem.radeef.ui.auth.RegisterViewModel
import com.saleem.radeef.util.toast
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SetFragment : PreferenceFragmentCompat() {
    val viewModel: RegisterViewModel by viewModels()

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        findPreference<Preference?>("logout")?.setOnPreferenceClickListener {
            toast("button clicked")
            viewModel.signOut()
            true
        }

//        viewModel.logout.observe(viewLifecycleOwner) {
//            val action = SetFragmentDirections.actionSetFragmentToEnterNumberFragment()
//            findNavController().navigate(action)
//        }
    }
}