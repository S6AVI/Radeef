package com.saleem.radeef.driver

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.saleem.radeef.AuthNavigationDirections
import com.saleem.radeef.R
import com.saleem.radeef.data.firestore.driver.RegistrationStatus
import com.saleem.radeef.driver.ui.register.info.DriverInfoFragmentDirections
import com.saleem.radeef.driver.ui.register.license.DriverLicenseFragmentDirections
import com.saleem.radeef.util.logD
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DriverMainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private val preferences: SharedPreferences by lazy {
        getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driver_main)


        val currentPageName =
            preferences.getString("current_page", RegistrationStatus.LICENSE.value)

        logD("currentPageName: $currentPageName")


        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.driver_nav_host_fragment) as NavHostFragment

        navController = navHostFragment.findNavController()

    }

    private fun getNextScreen(currentPage: RegistrationStatus): RegistrationStatus {
        return when (currentPage) {
            RegistrationStatus.INFO -> RegistrationStatus.INFO
            RegistrationStatus.LICENSE -> RegistrationStatus.LICENSE
            RegistrationStatus.VEHICLE -> RegistrationStatus.VEHICLE
            RegistrationStatus.COMPLETED -> RegistrationStatus.COMPLETED
        }
    }

}