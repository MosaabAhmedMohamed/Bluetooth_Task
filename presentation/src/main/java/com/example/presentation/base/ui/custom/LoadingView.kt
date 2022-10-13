package com.example.presentation.base.ui.custom

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.example.presentation.R
import com.example.presentation.base.ui.ext.visibility


class LoadingView : FrameLayout {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(attrs)
    }

    private fun init(attrs: AttributeSet) {
        inflate(context, R.layout.item_loading, this)
        handleVisibility(false)

    }

    fun handleVisibility(visibility: Boolean) {
        this.visibility(visibility)
    }

}

