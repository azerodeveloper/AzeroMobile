package com.soundai.azero.azeromobile.impl.contactingestion.PhoneConnectionStateManager.ContactIngestionAsyncTasks;

import com.azero.sdk.impl.ContactIngestion.ContactInputSourceType;
import com.azero.sdk.impl.ContactIngestion.ContactPojos.Contact;
import com.azero.sdk.impl.ContactIngestion.ContactUploader.ContactUploaderHandler;
import com.azero.sdk.util.log;

import java.util.List;

/**
 * The asynchronous task to upload contacts from a connected phone.
 */
public class UploadContactsTask implements Runnable {
    private List<Contact> contactList;
    private final ContactUploaderHandler mContactUploaderHandler;
    private final ContactInputSourceType mContactInputSourceType;

    public UploadContactsTask(List<Contact> contacts, final ContactInputSourceType contactInputSourceType,
                              final ContactUploaderHandler contactUploaderHandler) {
        this.contactList = contacts;
        mContactUploaderHandler = contactUploaderHandler;
        mContactInputSourceType = contactInputSourceType;
    }

    @Override
    public void run() {
        final boolean canStartUploadingContacts = mContactUploaderHandler.onBeginAddContact();
        if (canStartUploadingContacts) {
            try {
                log.i("Retrieved a contact list of size : " + contactList.size());
                for (int i = 0; i < contactList.size(); i++) {
                    if (!Thread.currentThread().isInterrupted()) {
                        final Contact contact = contactList.get(i);
                        final boolean isUploadedSuccessfully = mContactUploaderHandler.onAddContact(contact);
                        if (isLastContactToUpload(i, contactList.size())) {
                            mContactUploaderHandler.onEndAddContact();
                            log.i("All of the contacts are prepared to be uploaded from the connected phone.");
                        }
                    } else {
                        log.i("Uploading thread is interrupted.");
                        break;
                    }
                }
            } catch (final Exception ex) {
                log.e("Failed to retrieve contacts from contact store, " + ex);
            }
        } else {
            log.e("Failed to start uploading contacts.");
        }
    }

    private boolean isLastContactToUpload(final int currentIndex, final int contactListSize) {
        return currentIndex == (contactListSize - 1);
    }
}
