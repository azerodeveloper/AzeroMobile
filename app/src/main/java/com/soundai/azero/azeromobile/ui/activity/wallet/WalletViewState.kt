package com.soundai.azero.azeromobile.ui.activity.wallet

data class WalletViewState(
    val isLoading: Boolean,
    val throwable: Throwable?
) {

    companion object {

        fun initial(): WalletViewState {
            return WalletViewState(
                isLoading = false,
                throwable = null
            )
        }
    }
}