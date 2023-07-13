package com.saleem.radeef.ui.map
//
//import android.os.Bundle
//import android.view.View
//import androidx.appcompat.app.ActionBarDrawerToggle
//import androidx.drawerlayout.widget.DrawerLayout
//import androidx.fragment.app.Fragment
//import androidx.navigation.fragment.findNavController
//import com.google.android.material.navigation.NavigationView
//import com.saleem.radeef.R
//import com.saleem.radeef.databinding.FragmentNavigationDrawerBinding
//import com.saleem.radeef.util.exhaustive
//import com.saleem.radeef.util.toast
//
//class NavigationDrawerFragment : Fragment(R.layout.fragment_navigation_drawer) {
//
//    lateinit var binding: FragmentNavigationDrawerBinding
//
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        binding = FragmentNavigationDrawerBinding.bind(view)
//
//        val toggle = ActionBarDrawerToggle(
//            requireActivity(),
//            binding.drawerLayout,
//            R.string.open,
//            R.string.close
//        )
//        binding.drawerLayout.addDrawerListener(toggle)
//        toggle.syncState()
//    }
//
//}