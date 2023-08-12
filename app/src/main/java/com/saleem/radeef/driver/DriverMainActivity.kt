package com.saleem.radeef.driver

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.saleem.radeef.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DriverMainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driver_main)


        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.driver_nav_host_fragment) as NavHostFragment

        navController = navHostFragment.findNavController()

    }

}