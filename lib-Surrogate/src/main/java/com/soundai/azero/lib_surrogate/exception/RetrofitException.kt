package com.soundai.azero.lib_surrogate.exception

data class RetrofitException(val code: Int, val msg: String) : Throwable(msg)

fun convertException(e: Throwable): RetrofitException =
    when (e) {
        is RetrofitException -> e
        else -> RetrofitException(-1, e.message ?: "")
    }