package com.example.presentation.base.ui.ext

import android.view.View

fun View.gone() {
    if (visibility == View.VISIBLE)
        visibility = View.GONE
}

fun View.visible() {
    if (visibility == View.GONE)
        visibility = View.VISIBLE
}

fun View.visibility(show: Boolean) {
    visibility = if (show)
        View.VISIBLE
    else
        View.GONE
}
