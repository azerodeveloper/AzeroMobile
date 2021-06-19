package com.soundai.azero.azeromobile.impl.contactingestion.PhoneConnectionStateManager;

import com.azero.sdk.impl.ContactIngestion.ContactUploader.ContactUploaderHandler;

/**
 * The factory class to produce the state-manager classes based on the connectivity of the phone.
 */
public class PhoneConnectionStateManagerFactory {

    /**
     * Factory method to return a singleton instance of {@link PhoneConnectionStateManager}.
     * @param isPhoneConnected a boolean representing whether the phone is connected or not
     * @param contactUploaderHandler the {@link ContactUploaderHandler}
     * @return a singleton instance of {@link PhoneConnectionStateManager}
     */
    public static final PhoneConnectionStateManager getPhoneConnectionStateManager(
            final boolean isPhoneConnected,
            final ContactUploaderHandler contactUploaderHandler) {
        if (isPhoneConnected) {
            return PhoneConnectedStateManager.getInstance(contactUploaderHandler);
        }
        return PhoneDisconnectedStateManager.getInstance(contactUploaderHandler);
    }
}
