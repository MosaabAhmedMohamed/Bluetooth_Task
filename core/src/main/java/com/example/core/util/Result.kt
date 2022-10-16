package com.example.core.util

import androidx.annotation.Keep
import java.lang.Exception

@Keep
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class GeneralError(val exception: Exception? = null, val message: String? = null) : Result<Nothing>()
}
