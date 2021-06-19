package com.soundai.azero.azeromobile.manager

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlin.coroutines.CoroutineContext

val coroutineExceptionHandler = CoroutineExceptionHandler { coroutineContext, exception ->
    println("Coroutine in $coroutineContext Caught original $exception")
}