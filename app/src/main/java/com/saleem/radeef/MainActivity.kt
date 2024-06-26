package com.saleem.radeef

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.firebase.appcheck.ktx.appCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.initialize
import com.saleem.radeef.databinding.ActivityMainBinding
import com.saleem.radeef.driver.ui.DriverMainActivity
import com.saleem.radeef.passenger.ui.PassengerMainActivity
import com.saleem.radeef.util.Constants.USER_TYPE
import com.saleem.radeef.util.UserType
import com.saleem.radeef.util.logD
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private val preferences: SharedPreferences by lazy {
        getSharedPreferences("MyPreferences", Context.MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setTheme(R.style.AppTheme)

        binding = ActivityMainBinding.inflate(layoutInflater)
        val rootView = binding.root
        setContentView(rootView)

        // handle the two possible types
        if (isTypeSpecified()) {
            redirectToScreen()
        } else {
            setupButtonClickListeners()
        }

        // app check api
        Firebase.initialize(context = this)
        Firebase.appCheck.installAppCheckProviderFactory(
            PlayIntegrityAppCheckProviderFactory.getInstance(),
        )

    }

    private fun setupButtonClickListeners() {
        binding.passengerBtn.setOnClickListener {
            saveUserType(UserType.PASSENGER.name)
            redirectToScreen()
        }

        binding.driverBtn.setOnClickListener {
            saveUserType(UserType.DRIVER.name)
            redirectToScreen()
        }
    }

    private fun saveUserType(userType: String) {
        val editor = preferences.edit()
        editor.putString(USER_TYPE, userType)
        editor.apply()
    }


    private fun isTypeSpecified(): Boolean {
        return preferences.contains(USER_TYPE)
    }


    private fun redirectToScreen() {
        val userType = preferences.getString(USER_TYPE, null)
        logD("user type: $userType")

        userType?.let {
            val intent = when (userType) {
                UserType.PASSENGER.name -> Intent(this, PassengerMainActivity::class.java)
                else -> {
                    Intent(this, DriverMainActivity::class.java)
                }
            }
            startActivity(intent)
            finish()

        }
    }
}