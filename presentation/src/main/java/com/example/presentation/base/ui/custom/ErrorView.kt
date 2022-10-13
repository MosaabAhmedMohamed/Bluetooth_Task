package com.example.presentation.base.ui.custom

import android.content.Context
import com.example.presentation.R
import com.example.presentation.base.ui.ext.visibility
import android.util.AttributeSet
import android.widget.Button
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout

class ErrorView : ConstraintLayout {

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
        inflate(context, R.layout.item_error_message, this)
        handleVisibility(false)
        this.setBackgroundResource(android.R.color.transparent)

    }

    fun handleVisibility(visibility: Boolean) {
        this.visibility(visibility)
    }

    fun errorMessage(msg: String) {
        if (msg.isNotEmpty())
            this.findViewById<TextView>(R.id.message_tv).text = msg
        else {
            this.findViewById<TextView>(R.id.message_tv).text =
                context.getString(R.string.error_desc)
        }
    }

    fun emptyState(msg: String) {
        visibility(true)
        this.findViewById<TextView>(R.id.message_tv).text = msg
        this.findViewById<Button>(R.id.btn_retry).visibility(false)
    }

    fun downloadFailedState(msg: String) {
        visibility(true)
        this.findViewById<TextView>(R.id.message_tv).text = msg
        this.findViewById<Button>(R.id.btn_retry).text = context.getString(R.string.dismiss)
    }

    fun setOnClickListener(onClick: () -> Unit) {
        this.findViewById<Button>(R.id.btn_retry).setOnClickListener {
            onClick.invoke()
        }
    }

}