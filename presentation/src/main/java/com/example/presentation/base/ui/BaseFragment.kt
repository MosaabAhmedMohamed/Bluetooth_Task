package com.example.presentation.base.ui

import android.os.Bundle
import android.view.View
import dagger.android.support.DaggerFragment

abstract class BaseFragment() : DaggerFragment() {


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        onViewClicked()

    }

    protected abstract fun init()
    protected open fun onViewClicked(){}




}