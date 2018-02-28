package com.ibm.mobilefirstplatform.clientsdk.android.analytics.internal.inAppFeedBack;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.ibm.mobilefirstplatform.clientsdk.android.core.api.Response;
import com.ibm.mobilefirstplatform.clientsdk.android.core.api.ResponseListener;
import com.ibm.mobilefirstplatform.clientsdk.android.logger.api.LogPersister;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;


/**
 * Created by rott on 2/17/16.
 */
public class SendButtonAction extends AlertDialog {


    public SendButtonAction(Activity activity) {
        super(activity);

        SendAppFeedback.sendLogsToServer(true);

        Toast toast = Toast.makeText(activity, "Thanks for the feedback, you make our app better!", Toast.LENGTH_LONG);
        ViewGroup group = (ViewGroup) toast.getView();
        TextView messageTextView = (TextView) group.getChildAt(0);
        messageTextView.setTextSize(25);
        messageTextView.setTextColor(Color.BLUE);
        toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER, 0, 0);
        toast.show();

        //this.setTitle("App Feedback Sent");
        //this.setMessage("Thanks for the feedback, you make our app better!");
        //this.setButton(AlertDialog.BUTTON_POSITIVE, "OK,GOT IT", new PositiveButtonClick(activity));
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

}
