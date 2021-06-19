package com.soundai.azero.azeromobile.impl.contactingestion.PhoneConnectionStateManager;

import com.azero.platforms.contactuploader.ContactUploader;
import com.azero.sdk.impl.ContactIngestion.ContactInputSourceType;
import com.azero.sdk.impl.ContactIngestion.ContactPojos.Contact;
import com.azero.sdk.impl.ContactIngestion.ContactUploadAction;
import com.azero.sdk.impl.ContactIngestion.ContactUploader.ContactUploaderHandler;
import com.azero.sdk.util.log;
import com.soundai.azero.azeromobile.impl.contactingestion.PhoneConnectionStateManager.ContactIngestionAsyncTasks.CancelUploadContactsTask;
import com.soundai.azero.azeromobile.impl.contactingestion.PhoneConnectionStateManager.ContactIngestionAsyncTasks.RemoveUploadedContactsTask;
import com.soundai.azero.azeromobile.impl.contactingestion.PhoneConnectionStateManager.ContactIngestionAsyncTasks.UploadContactsTask;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static com.azero.sdk.impl.ContactIngestion.ContactUploadAction.*;

/**
 * The base class initializing the basic attributes of state manager classes of Phone-Connection.
 */
public final class PhoneConnectionStateManagerHelper {

    private static PhoneConnectionStateManagerHelper instance;

    private ExecutorService mExecutorService;
    private Map<ContactUploadAction, Future> mContactUploadTaskMap;
    private ContactUploader.ContactUploadStatus mCurrentContactUploaderStatus;

    private long mUploadTimeStart;
    private long mUploadTimeEnd;
    private long mRemoveTimeStart;
    private long mRemoveTimeEnd;

    private PhoneConnectionStateManagerHelper() {
        mContactUploadTaskMap = new ConcurrentHashMap<>();
        mExecutorService = Executors.newSingleThreadExecutor();
        mCurrentContactUploaderStatus = ContactUploader.ContactUploadStatus.UNKNOWN_ERROR;
        resetTimers();
        initializeContactUploadViewsByIds();
    }

    /**
     * Method to return singleton instance of {@link PhoneConnectionStateManagerHelper}.
     *
     * @return the singleton instance of {@link PhoneConnectionStateManagerHelper}.
     */
    public static PhoneConnectionStateManagerHelper getInstance() {
        if (instance == null) {
            instance = new PhoneConnectionStateManagerHelper();
        }
        return instance;
    }

    /**
     * Initializes the UI components of Contact-Uploader.
     */
    private void initializeContactUploadViewsByIds() {
    }

    /**
     * Make the contact upload progressbar visible.
     */
    public void showProgressBar() {
    }

    /**
     * Hide the contact-upload progressbar.
     */
    public void hideProgressBar() {
    }

    /**
     * Update the name of Contact-Upload button.
     *
     * @param actionNameResId the name of the action which is one of UPLOAD or CANCEL or REMOVE.
     */
    public void updateContactIngestionButtonWithActionName(final int actionNameResId) {
    }

    /**
     * Show the current contact ingestion status.
     *
     * @param ingestionStatusResId the status of contact ingestion.
     */
    public void showCurrentContactIngestionStatus(final int ingestionStatusResId) {
    }

    /**
     * Hiding the contact ingestion status.
     */
    public void hideContactIngestionStatus() {
    }

    /**
     * Remove the futures from the Action-to-future map.
     *
     * @param contactUploadActions the action states for which the map entry is needed to be removed.
     */
    public void removeFromFutureMapIfPresent(ContactUploadAction... contactUploadActions) {
        for (ContactUploadAction contactUploadAction : contactUploadActions) {
            if (mContactUploadTaskMap.containsKey(contactUploadAction)) {
                mContactUploadTaskMap.remove(contactUploadAction);
            }
        }
    }

    /**
     * Updates the Contact-Uploader UI on denial of access.
     */
    public void updateViewOnContactAccessDenial() {
    }

    /**
     * Updates te Contact-Uploader UI on start up.
     */
    public void showInitialViewOnContactBoard() {
    }

    /**
     * Updates the view of Contact-Uploader on Confirmation of access.
     */
    public void updateViewOnContactAccessConfirmation() {
    }

    /**
     * Interrupt the contact uploader sync task based on the action states.
     *
     * @param contactUploadActions the {@link ContactUploadAction}s to interrupt.
     */
    public void interruptContactUploadActionTasks(final ContactUploadAction... contactUploadActions) {
        for (ContactUploadAction contactUploadAction : contactUploadActions) {
            if (mContactUploadTaskMap.containsKey(contactUploadAction)) {
                final Future future = mContactUploadTaskMap.get(contactUploadAction);
                if (!future.isDone() || !future.isCancelled()) {
                    future.cancel(true);
                }
                mContactUploadTaskMap.remove(contactUploadAction);
            }
        }
    }

    /**
     * Helper method to start uploading contacts from a specified contact-source.
     */
    public void startUploadingContacts(
            final List<Contact> contacts,
            final ContactInputSourceType contactInputSourceType,
            final ContactUploaderHandler contactUploaderHandler) {
        submitContactUploadTask(UPLOAD, new UploadContactsTask(contacts, contactInputSourceType, contactUploaderHandler));
    }

    /**
     * Helper method to start cancelling contacts.
     */
    public void cancelUploadingContacts(
            final ContactUploaderHandler contactUploaderHandler) {
        interruptContactUploadActionTasks(UPLOAD);
        submitContactUploadTask(CANCEL, new CancelUploadContactsTask(contactUploaderHandler));
    }

    /**
     * Helper method to start removing contacts.
     */
    private void removeUploadedContacts(
            final ContactUploaderHandler contactUploaderHandler) {
        submitContactUploadTask(REMOVE, new RemoveUploadedContactsTask(contactUploaderHandler));
    }

    /**
     * Sets the onClickListeners for contact-upload button.
     */
    public void setOnclickListenerForUploadOrCancelButton(final ContactInputSourceType contactInputSourceType,
                                                          final ContactUploaderHandler contactUploaderHandler) {
//        mContactUploadButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                switch (ContactUploadAction.valueOf(String.valueOf(mContactUploadButton.getText()))) {
//                    case UPLOAD:
//                        disableContactUploaderButton();
//                        startUploadingContacts(contactInputSourceType, contactUploaderHandler);
//                        break;
//                    case CANCEL:
//                        disableContactUploaderButton();
//                        cancelUploadingContacts(contactUploaderHandler);
//                        break;
//                    case REMOVE:
//                        disableContactUploaderButton();
//                        removeUploadedContacts(contactUploaderHandler);
//                        break;
//                }
//            }
//        });
    }

    /**
     * Helper method to delete uploaded contacts.
     */
    public void deleteContacts(final ContactUploaderHandler contactUploaderHandler) {
        if (mCurrentContactUploaderStatus != null
                && (mCurrentContactUploaderStatus == ContactUploader.ContactUploadStatus.UPLOAD_CONTACTS_STARTED
                || mCurrentContactUploaderStatus == ContactUploader.ContactUploadStatus.UPLOAD_CONTACTS_UPLOADING)) {
            cancelUploadingContacts(contactUploaderHandler);
        } else {
            removeUploadedContacts(contactUploaderHandler);
        }
    }

    /**
     * Submitting contact uploading tasks for execution.
     *
     * @param contactUploadAction a {@link ContactUploadAction}
     * @param contactUploadTask   the task i.e. contact upload, cancel upload or remove upload to execute
     */
    public void submitContactUploadTask(final ContactUploadAction contactUploadAction,
                                        final Runnable contactUploadTask) {
        mContactUploadTaskMap.put(contactUploadAction,
                mExecutorService.submit(contactUploadTask));
    }

    /**
     * Disabling the contact uploader button.
     */
    public void disableContactUploaderButton() {
    }

    /**
     * Enabling the contact-uploader button.
     */
    public void enableContactUploaderButton() {
    }

    /**
     * Tracks the current contact-upload status.
     *
     * @param contactUploadStatus
     */
    public void updateContactUploaderState(final ContactUploader.ContactUploadStatus contactUploadStatus) {
        mCurrentContactUploaderStatus = contactUploadStatus;
    }

    public void resetTimers() {
        resetContactsUploadTimers();
        resetContactRemoveTimers();
    }

    public void resetContactsUploadTimers() {
        mUploadTimeStart = mUploadTimeEnd = -1;
    }

    public void resetContactRemoveTimers() {
        mRemoveTimeStart = mRemoveTimeEnd = -1;
    }

    public void setStartUploadingContactTimeStamp(final long timeStampInMilliSeconds) {
        mUploadTimeStart = timeStampInMilliSeconds;
    }

    public void setFinishUploadingContactTimeStamp(final long timeStampInMilliSeconds) {
        mUploadTimeEnd = timeStampInMilliSeconds;
    }

    public void setStartRemovingContactTimeStamp(final long timeStampInMilliSeconds) {
        mRemoveTimeStart = timeStampInMilliSeconds;
    }

    public void setFinishRemovingContactTimeStamp(final long timeStampInMilliSeconds) {
        mRemoveTimeEnd = timeStampInMilliSeconds;
    }

    public void logTimes(final String tag) {
        if (mUploadTimeStart != -1 && mUploadTimeEnd != -1 && mUploadTimeEnd >= mUploadTimeStart) {
            log.i("Time taken to upload contacts is : " + (mUploadTimeEnd - mUploadTimeStart) / 1000 + " secs.");
        }
        if (mRemoveTimeStart != -1 && mRemoveTimeEnd != -1 && mRemoveTimeEnd >= mRemoveTimeStart) {
            log.i("Time taken to remove contacts is : " + (mUploadTimeEnd - mUploadTimeStart) / 1000 + " secs.");
        }
    }
}
