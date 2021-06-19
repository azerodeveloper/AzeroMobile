//package com.soundai.azero.azeromobile.impl.contactingestion
//
//import com.azero.platforms.contactuploader.ContactUploader
//import com.azero.sdk.util.log
//import com.soundai.azero.azeromobile.impl.contactingestion.PhoneConnectionStateManager.PhoneConnectionStateManager
//import com.soundai.azero.azeromobile.impl.contactingestion.PhoneConnectionStateManager.PhoneConnectionStateManagerFactory
//
//class ContactUploaderHandler : ContactUploader() {
//    private var mPhoneConnectionStateHandler: PhoneConnectionStateManager? = null
//
//    init {
//        handlePhoneConnectivityStates(true)
//    }
//
//    /**
//     * Invokes a state-manager class according to phone connection.
//     *
//     * @param isChecked a boolean representing whether a phone is connected or not
//     */
//    private fun handlePhoneConnectivityStates(isChecked: Boolean) {
//        mPhoneConnectionStateHandler =
//            PhoneConnectionStateManagerFactory.getPhoneConnectionStateManager(isChecked, this)
//        mPhoneConnectionStateHandler?.handleState()
//    }
//
//    override fun contactsUploaderStatusChanged(
//        contactUploadStatus: ContactUploadStatus?,
//        info: String?
//    ) {
//        log.d("contactUploadStatus:$contactUploadStatus,info:$info")
//    }
//}