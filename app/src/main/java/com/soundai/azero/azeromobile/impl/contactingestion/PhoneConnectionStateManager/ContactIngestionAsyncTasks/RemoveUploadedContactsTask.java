package com.soundai.azero.azeromobile.impl.contactingestion.PhoneConnectionStateManager.ContactIngestionAsyncTasks;

import com.azero.sdk.impl.ContactIngestion.ContactUploader.ContactUploaderHandler;
import com.azero.sdk.util.log;

/**
 * The asynchronous task to remove all of the uploaded address-books from a connected phone.
 */
public class RemoveUploadedContactsTask implements Runnable {
    final ContactUploaderHandler mContactUploaderHandler;

    public RemoveUploadedContactsTask(final ContactUploaderHandler contactUploaderHandler) {
        mContactUploaderHandler = contactUploaderHandler;
    }

    @Override
    public void run() {
        mContactUploaderHandler.onClearContact();
        log.i( "Removing uploaded contacts.");
    }
}
