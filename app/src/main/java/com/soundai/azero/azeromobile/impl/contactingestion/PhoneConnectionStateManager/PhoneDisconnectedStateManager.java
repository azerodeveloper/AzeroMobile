package com.soundai.azero.azeromobile.impl.contactingestion.PhoneConnectionStateManager;

import com.azero.platforms.contactuploader.ContactUploader;
import com.azero.sdk.impl.ContactIngestion.ContactUploader.ContactUploaderHandler;
import com.azero.sdk.util.log;
/**
 * The state-manager class managing the state transitions of the Contact-Uploader UI when a Phone is disconnected.
 */
public class PhoneDisconnectedStateManager implements PhoneConnectionStateManager {

    private static final String sTag = PhoneDisconnectedStateManager.class.getSimpleName();
    private static PhoneDisconnectedStateManager instance;

    private ContactUploaderHandler mContactUploaderHandler;
    private PhoneConnectionStateManagerHelper mPhoneConnectionStateManagerHelper;

    private PhoneDisconnectedStateManager(final ContactUploaderHandler contactUploaderHandler) {
        mContactUploaderHandler = contactUploaderHandler;
        mPhoneConnectionStateManagerHelper = PhoneConnectionStateManagerHelper.getInstance();
    }

    /**
     * Method to get the singleton instance of {@link PhoneDisconnectedStateManager}.
     *
     * @param contactUploaderHandler the {@link ContactUploaderHandler}
     * @return the singleton instance of {@link PhoneDisconnectedStateManager}
     */
    public static PhoneDisconnectedStateManager getInstance(final ContactUploaderHandler contactUploaderHandler) {
        if (instance == null) {
            instance = new PhoneDisconnectedStateManager(contactUploaderHandler);
        }
        return instance;
    }

    /**
     * Handles states of Contact-Uploader upon connecting or disconnecting a phone.
     */
    @Override
    public void handleState() {
        mPhoneConnectionStateManagerHelper.showInitialViewOnContactBoard();
        mPhoneConnectionStateManagerHelper.deleteContacts(mContactUploaderHandler);
    }

    /**
     * The callback function from engine upon detecting a change in contact upload status.
     *
     * @param contactUploadStatus the current status of contact upload, {@link ContactUploader.ContactUploadStatus}
     * @param jsonReason          the reason of failure
     */
    @Override
    public void onContactUploadStatusChanged(final ContactUploader.ContactUploadStatus contactUploadStatus, final String jsonReason) {
        log.i("Received contact ingestion callback status as " + contactUploadStatus.toString() + " from engine ");
        mPhoneConnectionStateManagerHelper.updateContactUploaderState(contactUploadStatus);
        mPhoneConnectionStateManagerHelper.hideProgressBar();
        mPhoneConnectionStateManagerHelper.hideContactIngestionStatus();

        if (contactUploadStatus == ContactUploader.ContactUploadStatus.REMOVE_CONTACTS_STARTED) {
            mPhoneConnectionStateManagerHelper.resetTimers();
            mPhoneConnectionStateManagerHelper.setStartRemovingContactTimeStamp(System.currentTimeMillis());
        }

        if (contactUploadStatus == ContactUploader.ContactUploadStatus.REMOVE_CONTACTS_COMPLETED) {
            mPhoneConnectionStateManagerHelper.setFinishRemovingContactTimeStamp(System.currentTimeMillis());
            mPhoneConnectionStateManagerHelper.logTimes(sTag);
        }
    }
}
