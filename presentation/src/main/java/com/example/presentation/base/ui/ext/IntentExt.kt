package com.example.presentation.base.ui.ext

import android.content.Context
import android.content.Intent
import android.net.Uri


fun Context.openVideoIntent(fileUri : Uri) {
    val intent = Intent(Intent.ACTION_VIEW)
    intent.setDataAndType(fileUri, "video/*")
    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    startActivity(intent)
}

fun Context.openPDFIntent(fileUri : Uri) {
    val intent = Intent(Intent.ACTION_VIEW)
    intent.setDataAndType(fileUri, "application/pdf")
    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    startActivity(intent)
}


