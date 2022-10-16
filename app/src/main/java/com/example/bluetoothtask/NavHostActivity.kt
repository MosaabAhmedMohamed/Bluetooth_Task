package com.example.bluetoothtask

import android.os.Bundle
import com.example.presentation.base.ui.BaseActivity
import com.example.presentation.base.ui.ext.initNavManager
import com.example.presentation.databinding.ActivityNavHostBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NavHostActivity: BaseActivity() {

    private var binding: ActivityNavHostBinding? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNavHostBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        initNavManager()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}