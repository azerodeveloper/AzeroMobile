package com.soundai.azero.azeromobile.impl.contactingestion.PhoneConnectionStateManager.ContactIngestionAsyncTasks;

import com.azero.sdk.impl.ContactIngestion.ContactUploader.ContactUploaderHandler;
import com.azero.sdk.util.log;

/**
 * The asynchronous task to abort uploading of contacts from the connected phone.
 */
public class CancelUploadContactsTask implements Runnable {

    private final String sTag = CancelUploadContactsTask.class.getSimpleName();

    final ContactUploaderHandler mContactUploaderHandler;

    public CancelUploadContactsTask(final ContactUploaderHandler contactUploaderHandler) {
        mContactUploaderHandler = contactUploaderHandler;
    }

    @Override
    public void run() {
        log.i( "Calling engine to cancel contact upload.");
            final boolean cancelUpload = mContactUploaderHandler.onAddContactsCancel();
            if (cancelUpload) {
                log.i( "Started cancelling contact upload.");
            } else {
                log.e("Failed to cancel contact upload.");
            }
    }
}
