package com.soundai.azero.lib_todayrunrecord

import android.util.Log
import kotlinx.coroutines.CoroutineExceptionHandler

val coroutineExceptionHandler = CoroutineExceptionHandler { coroutineContext, exception ->
    Log.e("Coroutine","in $coroutineContext Caught original $exception")
}