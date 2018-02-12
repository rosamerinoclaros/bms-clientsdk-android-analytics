package com.ibm.mobilefirstplatform.clientsdk.android.analytics.internal.inAppFeedBack;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;


/**
 * Created by rott on 2/17/16.
 */
public class DismissAppFeedBack extends AlertDialog {

    /**
     * Clicking "YES,DISMISS"
     */
    private static class SaveButtonClick implements OnClickListener {
        private Activity activity;

        public SaveButtonClick(Activity activity) {
            this.activity = activity;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            activity.finish();
        }
    }

    /**
     * Clicking "YES,DISMISS"
     */
    private static class DiscardButtonClick implements OnClickListener {
        private Activity activity;
        private String filename;

        public DiscardButtonClick(Activity activity, String filename) {
            this.activity = activity;
            this.filename = filename;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            //Discard the changes and close the feedback activity
            if(filename.equals("ALL")){
                Utility.deleteAllFiles();
            }else {
                Utility.discardFeedbackFiles(filename);
            }
            activity.finish();
        }
    }

    /**
     * Clicking "NO,CANCEL"
     */
    private static class NegativeButtonClick implements OnClickListener {
        private Activity activity;

        public NegativeButtonClick(Activity activity) {
            this.activity = activity;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            //Do nothing
        }
    }

    public DismissAppFeedBack(Activity activity, String filename, boolean isEdited) {
        super(activity);

        if(isEdited){
            this.setTitle("Dismiss App Feedback?");
            //this.setMessage("All data & comments made will be deleted. \n Do you want to continue?");
            this.setMessage("Do you want to SAVE/DISCARD the changes before Exit?");
            this.setButton(AlertDialog.BUTTON_NEGATIVE, "DISCARD,DISMISS", new DiscardButtonClick(activity, filename));
            this.setButton(AlertDialog.BUTTON_NEUTRAL, "SAVE,DISMISS", new SaveButtonClick(activity));
            this.setButton(AlertDialog.BUTTON_POSITIVE, "NO,CANCEL", new NegativeButtonClick(activity));
        } else {
            this.setTitle("Dismiss App Feedback?");
            this.setMessage("All data & comments made will be deleted. \n Do you want to continue?");
            this.setButton(AlertDialog.BUTTON_NEGATIVE, "YES,DISMISS", new DiscardButtonClick(activity, filename));
            this.setButton(AlertDialog.BUTTON_POSITIVE, "NO,CANCEL", new NegativeButtonClick(activity));
        }
    }

}
