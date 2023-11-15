package com.saleem.radeef.passenger.ui

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.saleem.radeef.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PassengerMainActivity : AppCompatActivity() {
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setTheme(R.style.AppTheme)

        setContentView(R.layout.passenger_activity_main)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        navController = navHostFragment.findNavController()

        // Set up the back button behavior

        val callback = object : OnBackPressedCallback(true /* enabled by default */) {
            override fun handleOnBackPressed() {
                // Close the current activity and exit the app
                finish()
            }
        }
        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            callback.isEnabled = destination.id == R.id.homeFragment
        }
        onBackPressedDispatcher.addCallback(this, callback)
    }



}