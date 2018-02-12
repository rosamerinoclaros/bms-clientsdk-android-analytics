package com.ibm.mobilefirstplatform.clientsdk.android.analytics.internal.inAppFeedBack;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;


/**
 * Created by rott on 2/17/16.
 */
public class SubmitAppFeedBack extends AlertDialog {

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


    public SubmitAppFeedBack(Activity activity) {
        super(activity);

        Utility.sendLogsToServer();
        this.setTitle("App Feedback Sent");
        this.setMessage("Thanks for the feedback, you make our app better!");
        this.setButton(AlertDialog.BUTTON_POSITIVE, "OK,GOT IT", new PositiveButtonClick(activity));
    }

}
