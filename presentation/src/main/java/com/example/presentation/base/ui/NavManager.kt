package com.example.presentation.base.ui

import androidx.navigation.NavDirections



object NavManager {
    private var navEventListener: ((navDirections: NavDirections) -> Unit)? = null

    fun navigate(navDirections: NavDirections) {
        navEventListener?.let {
            it.invoke(navDirections)
        }
    }

    fun setOnNavEvent(navEventListener: (navDirections: NavDirections) -> Unit) {
        NavManager.navEventListener = navEventListener
    }

}

