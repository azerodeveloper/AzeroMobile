package com.soundai.azero.lib_surrogate.request

data class WithdrawalRequest(
    val amount: Long,
    val applyNo: String,
    val appId: String,
    val desc: String,
    val sign: String,
    val code: String
)