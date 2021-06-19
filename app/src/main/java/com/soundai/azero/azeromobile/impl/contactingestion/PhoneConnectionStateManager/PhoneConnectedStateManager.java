package com.soundai.azero.azeromobile.impl.contactingestion.PhoneConnectionStateManager;

import com.azero.platforms.contactuploader.ContactUploader;
import com.azero.sdk.impl.ContactIngestion.ContactInputSourceType;
import com.azero.sdk.impl.ContactIngestion.ContactUploader.ContactUploaderHandler;
import com.azero.sdk.util.log;

/**
 * The state-manager class managing the UI state transitions upon availability of a connected phone.
 */
public class PhoneConnectedStateManager implements PhoneConnectionStateManager {

    private static final String sTag = PhoneConnectedStateManager.class.getSimpleName();
    private static PhoneConnectedStateManager instance;

    private PhoneConnectionStateManagerHelper mPhoneConnectionStateManagerHelper;
    private ContactUploaderHandler mContactUploaderHandler;
    private ContactInputSourceType mContactInputSourceType;

    private PhoneConnectedStateManager(final ContactUploaderHandler contactUploaderHandler) {
        mContactUploaderHandler = contactUploaderHandler;
        mPhoneConnectionStateManagerHelper = PhoneConnectionStateManagerHelper.getInstance();
        mContactInputSourceType = ContactInputSourceType.FILE;
    }

    /**
     * Method to get the singleton instance of {@link PhoneConnectedStateManager}.
     * @param contactUploaderHandler the {@link ContactUploaderHandler}
     * @return the singleton instance of {@link PhoneConnectedStateManager}
     */
    public static PhoneConnectedStateManager getInstance(final ContactUploaderHandler contactUploaderHandler) {
        if (instance == null) {
            instance = new PhoneConnectedStateManager(contactUploaderHandler);
        }
        return instance;
    }

    /**
     * Handles states of Contact-Uploader upon connecting or disconnecting a phone.
     */
    @Override
    public void handleState() {
        showPermissionDialogToAccessContacts();
        mPhoneConnectionStateManagerHelper.setOnclickListenerForUploadOrCancelButton(mContactInputSourceType, mContactUploaderHandler);
    }

    /**
     * Shows a dialog to request permissions to access contacts.
     */
    private void showPermissionDialogToAccessContacts() {
//        final DialogInterface.OnClickListener clickListener = new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                switch (which) {
//                    case BUTTON_POSITIVE:
//                        mPhoneConnectionStateManagerHelper.updateViewOnContactAccessConfirmation();
//                        break;
//                    case BUTTON_NEGATIVE:
//                        View switchItem = mActivity.findViewById( R.id.togglePhoneConnection );
//                        SwitchCompat contactUploadSwitch = switchItem.findViewById( R.id.drawerSwitch );
//                        contactUploadSwitch.setChecked( false );
//                        mPhoneConnectionStateManagerHelper.updateViewOnContactAccessDenial();
//                        break;
//                }
//            }
//        };
//
//        new AlertDialog.Builder(mActivity)
//            .setMessage(R.string.seek_access_to_contacts)
//            .setPositiveButton(R.string.confirm, clickListener)
//            .setNegativeButton(R.string.deny, clickListener)
//            .create()
//            .show();
    }

    /**
     * The callback function from engine upon detecting a change in contact upload status.
     * @param contactUploadStatus the current status of contact upload, {@link ContactUploader.ContactUploadStatus}
     * @param jsonReason the reason of failure
     */
    @Override
    public void onContactUploadStatusChanged(final ContactUploader.ContactUploadStatus contactUploadStatus,
                                             final String jsonReason) {
        log.i( "Contact Upload Status Changed. STATUS: " + contactUploadStatus.toString());
        mPhoneConnectionStateManagerHelper.updateContactUploaderState(contactUploadStatus);
//        mActivity.runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                switch (contactUploadStatus) {
//                    case UPLOAD_CONTACTS_STARTED:
//                        mPhoneConnectionStateManagerHelper.resetTimers();
//                        mPhoneConnectionStateManagerHelper.setStartUploadingContactTimeStamp(System.currentTimeMillis());
//                        mPhoneConnectionStateManagerHelper.enableContactUploaderButton();
//                        mPhoneConnectionStateManagerHelper.showProgressBar();
//                        mPhoneConnectionStateManagerHelper.updateContactIngestionButtonWithActionName(R.string.cancel_contacts);
//                        break;
//                    case UPLOAD_CONTACTS_UPLOADING:
//                        mPhoneConnectionStateManagerHelper.enableContactUploaderButton();
//                        mPhoneConnectionStateManagerHelper.showProgressBar();
//                        mPhoneConnectionStateManagerHelper.showCurrentContactIngestionStatus(R.string.uploading_contacts);
//                        mPhoneConnectionStateManagerHelper.updateContactIngestionButtonWithActionName(R.string.cancel_contacts);
//                        break;
//                    case UPLOAD_CONTACTS_COMPLETED:
//                        mPhoneConnectionStateManagerHelper.setFinishUploadingContactTimeStamp(System.currentTimeMillis());
//                        mPhoneConnectionStateManagerHelper.enableContactUploaderButton();
//                        mPhoneConnectionStateManagerHelper.hideProgressBar();
//                        mPhoneConnectionStateManagerHelper.showCurrentContactIngestionStatus(R.string.upload_contacts_successful);
//                        mPhoneConnectionStateManagerHelper.updateContactIngestionButtonWithActionName(R.string.remove_contacts);
//                        mPhoneConnectionStateManagerHelper.removeFromFutureMapIfPresent(UPLOAD);
//                        mPhoneConnectionStateManagerHelper.logTimes(mLogger, sTag);
//                        break;
//                    case UPLOAD_CONTACTS_ERROR:
//                        mPhoneConnectionStateManagerHelper.resetContactsUploadTimers();
//                        mPhoneConnectionStateManagerHelper.enableContactUploaderButton();
//                        mPhoneConnectionStateManagerHelper.hideProgressBar();
//                        mPhoneConnectionStateManagerHelper.showCurrentContactIngestionStatus(R.string.upload_contacts_failure);
//                        mPhoneConnectionStateManagerHelper.updateContactIngestionButtonWithActionName(R.string.upload_contacts);
//                        mPhoneConnectionStateManagerHelper.removeFromFutureMapIfPresent(UPLOAD);
//                        break;
//                    case UPLOAD_CONTACTS_CANCELED:
//                        mPhoneConnectionStateManagerHelper.resetTimers();
//                        mPhoneConnectionStateManagerHelper.enableContactUploaderButton();
//                        mPhoneConnectionStateManagerHelper.hideProgressBar();
//                        mPhoneConnectionStateManagerHelper.showCurrentContactIngestionStatus(R.string.cancel_contacts_successful);
//                        mPhoneConnectionStateManagerHelper.updateContactIngestionButtonWithActionName(R.string.upload_contacts);
//                        mPhoneConnectionStateManagerHelper.removeFromFutureMapIfPresent(UPLOAD, CANCEL);
//                        break;
//                    case REMOVE_CONTACTS_STARTED:
//                        mPhoneConnectionStateManagerHelper.resetTimers();
//                        mPhoneConnectionStateManagerHelper.setStartRemovingContactTimeStamp(System.currentTimeMillis());
//                        mPhoneConnectionStateManagerHelper.disableContactUploaderButton();
//                        mPhoneConnectionStateManagerHelper.showProgressBar();
//                        mPhoneConnectionStateManagerHelper.showCurrentContactIngestionStatus(R.string.removing_contacts);
//                        break;
//                    case REMOVE_CONTACTS_COMPLETED:
//                        mPhoneConnectionStateManagerHelper.setFinishRemovingContactTimeStamp(System.currentTimeMillis());
//                        mPhoneConnectionStateManagerHelper.enableContactUploaderButton();
//                        mPhoneConnectionStateManagerHelper.hideProgressBar();
//                        mPhoneConnectionStateManagerHelper.showCurrentContactIngestionStatus(R.string.remove_contacts_successful);
//                        mPhoneConnectionStateManagerHelper.updateContactIngestionButtonWithActionName(R.string.upload_contacts);
//                        mPhoneConnectionStateManagerHelper.removeFromFutureMapIfPresent(UPLOAD, REMOVE);
//                        mPhoneConnectionStateManagerHelper.logTimes(mLogger, sTag);
//                        break;
//                    case REMOVE_CONTACTS_ERROR:
//                        mPhoneConnectionStateManagerHelper.resetContactRemoveTimers();
//                        mPhoneConnectionStateManagerHelper.enableContactUploaderButton();
//                        mPhoneConnectionStateManagerHelper.hideProgressBar();
//                        mPhoneConnectionStateManagerHelper.showCurrentContactIngestionStatus(R.string.remove_contacts_failure);
//                        mPhoneConnectionStateManagerHelper.updateContactIngestionButtonWithActionName(R.string.upload_contacts);
//                        mPhoneConnectionStateManagerHelper.removeFromFutureMapIfPresent(REMOVE);
//                        break;
//                    case UNKNOWN_ERROR:
//                        mPhoneConnectionStateManagerHelper.resetTimers();
//                        mPhoneConnectionStateManagerHelper.enableContactUploaderButton();
//                        mPhoneConnectionStateManagerHelper.hideProgressBar();
//                        mPhoneConnectionStateManagerHelper.showCurrentContactIngestionStatus(R.string.failed_with_unknown_error);
//                        mPhoneConnectionStateManagerHelper.updateContactIngestionButtonWithActionName(R.string.upload_contacts);
//                        log.e("Contact upload failed due to " + jsonReason);
//                        break;
//                    default:
//                        mPhoneConnectionStateManagerHelper.resetTimers();
//                        mPhoneConnectionStateManagerHelper.hideProgressBar();
//                        mPhoneConnectionStateManagerHelper.hideContactIngestionStatus();
//                        mPhoneConnectionStateManagerHelper.enableContactUploaderButton();
//                        break;
//                }
//            }
//        });
    }

}
