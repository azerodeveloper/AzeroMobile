package com.soundai.azero.azeromobile.ui.activity.personal

data class PersonalInfoViewState(
    val isLoading: Boolean,
    val isUpdateSuccess: Boolean,
    val throwable: Throwable?
) {

    companion object {
        fun initial(): PersonalInfoViewState {
            return PersonalInfoViewState(
                isLoading = false,
                isUpdateSuccess = false,
                throwable = null
            )
        }
    }
}