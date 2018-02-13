package com.ibm.mobilefirstplatform.clientsdk.android.analytics.internal.inAppFeedBack;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;

/**
 * Created by mohlogan on 17/01/18.
 */

public class ReviewButtonAction extends AlertDialog{

    /**
     * Clicking "YES,DISMISS"
     */
    private static class Option1 implements OnClickListener {
        private Activity activity;
        private String filename;

        public Option1(Activity activity, String filename) {
            this.activity = activity;
            this.filename = filename;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            Intent intent = new Intent(activity, com.ibm.mobilefirstplatform.clientsdk.android.analytics.internal.inAppFeedBack.ReviewPopup.class);
            intent.putExtra("imagename", filename);
            activity.startActivityForResult(intent, 200);
        }
    }

    /**
     * Clicking "NO,DISMISS"
     */
    private static class Option2 implements OnClickListener {
        private Activity activity;
        private String filename;

        public Option2(Activity activity, String filename) {
            this.activity = activity;
            this.filename = filename;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            //Discard the changes and close the feedback activity
            Utility.discardFeedbackFiles(filename);

            String appFeedBackSummary = Utility.convertFileToString("AppFeedBackSummary.json");
            if ( appFeedBackSummary.equals("") || appFeedBackSummary.equals("{}") || appFeedBackSummary.contains("\"saved\":[]") ) {
                String alertReasonString = "No feedback comments available to review. \n Please go back and create feedback!";

                AlertDialog alertDialog = new AlertDialog.Builder(activity).create();
                alertDialog.setTitle("Alert");
                alertDialog.setMessage(alertReasonString);
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK,GOT IT", new PositiveButtonClick(activity));
                alertDialog.show();
            }
            Intent intent = new Intent(activity, com.ibm.mobilefirstplatform.clientsdk.android.analytics.internal.inAppFeedBack.ReviewPopup.class);
            //intent.putExtra("imagename", filename);
            activity.startActivityForResult(intent, 200);
        }
    }

    /**
     * Clicking "NO,CANCEL"
     */
    private static class Option3 implements OnClickListener {
        private Activity activity;

        public Option3(Activity activity) {
            this.activity = activity;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            //Do nothing
        }
    }

    /**
     * Clicking "OK, GOT IT"
     */
    private static class PositiveButtonClick implements OnClickListener {
        private Activity activity;

        public PositiveButtonClick(Activity activity) {
            this.activity = activity;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            activity.setResult(200,null);
            activity.finish();
        }
    }

    public ReviewButtonAction(Activity activity, String filename, boolean isEdited) {
        super(activity);

        String titleString = "Dismiss App Feedback?";
        String alertReasonString = "Do you want to SAVE/DISCARD the changes before Exit?";
        String optionString1 = "DISCARD,DISMISS";
        String optionString2 = "SAVE,DISMISS";
        String optionString3 = "NO,CANCEL";

        titleString = "Review App Feedback?";
        this.setTitle(titleString);

        String appFeedBackSummary = Utility.convertFileToString("AppFeedBackSummary.json");
        if ( appFeedBackSummary.equals("") || appFeedBackSummary.equals("{}") || appFeedBackSummary.contains("\"saved\":[]") ) {
            alertReasonString = "No feedback comments available to review. \n Please go back and create feedback!";

            this.setTitle("Alert");
            this.setMessage(alertReasonString);
            this.setButton(AlertDialog.BUTTON_POSITIVE, "OK,GOT IT", new PositiveButtonClick(activity));
        }else {
            if (isEdited) {
                alertReasonString = "All Data & comments made can be reviewed. \n Do you want to continue?";
                optionString1 = "SAVE, REVIEW";
                optionString2 = "DISCARD, REVIEW";
                optionString3 = "NO, CANCEL";

                this.setMessage(alertReasonString);
                this.setButton(AlertDialog.BUTTON_POSITIVE, optionString1, new Option1(activity, filename));
                this.setButton(AlertDialog.BUTTON_NEUTRAL, optionString2, new Option2(activity,filename));
                this.setButton(AlertDialog.BUTTON_NEGATIVE, optionString3, new Option3(activity));
            } else {
                alertReasonString = "No Comments. \n Do you want to review other feedback's?";
                optionString2 = "YES, REVIEW";
                optionString3 = "NO, CANCEL";

                this.setMessage(alertReasonString);
                this.setButton(AlertDialog.BUTTON_NEUTRAL, optionString2, new Option2(activity, filename));
                this.setButton(AlertDialog.BUTTON_NEGATIVE, optionString3, new Option3(activity));
            }
        }
    }
}
