package com.saleem.radeef.driver.ui.home

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.saleem.radeef.R
import com.saleem.radeef.databinding.DriverRequestFragmentBinding
import com.saleem.radeef.util.Permissions.hasLocationPermission
import com.saleem.radeef.util.Permissions.requestLocationPermission
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog

class DriverRequestPermissionFragment: Fragment(R.layout.driver_request_fragment), EasyPermissions.PermissionCallbacks {

    private lateinit var binding: DriverRequestFragmentBinding
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = DriverRequestFragmentBinding.bind(view)

        binding.continueBtn.setOnClickListener {
            if (hasLocationPermission(requireContext())) {
                navigateToHome()
            } else {
                requestLocationPermission(this)
            }
        }
    }



    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            SettingsDialog.Builder(requireActivity()).build().show()
        } else {
            requestLocationPermission(this)
        }

    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        navigateToHome()
    }

    private fun navigateToHome() {
        val action = DriverRequestPermissionFragmentDirections.actionDriverRequestPermissionFragmentToDriverHomeFragment()
        findNavController().navigate(action)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (hasLocationPermission(requireContext())) {
            navigateToHome()
        }
    }

}