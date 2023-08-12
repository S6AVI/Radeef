package com.saleem.radeef.driver.ui.upload_files

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.saleem.radeef.R
import com.saleem.radeef.databinding.UploadFilesFragmentBinding
import com.saleem.radeef.driver.ui.info.DriverInfoViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DriverUploadsFragment() : Fragment(R.layout.upload_files_fragment) {
    private lateinit var binding: UploadFilesFragmentBinding
    val viewModel: DriverUploadsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = UploadFilesFragmentBinding.bind(view)



    }


}