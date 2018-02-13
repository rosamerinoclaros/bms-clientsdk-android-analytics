package com.ibm.mobilefirstplatform.clientsdk.android.analytics.internal.inAppFeedBack;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;

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
public class SubmitAppFeedBack extends AlertDialog {


    public SubmitAppFeedBack(Activity activity) {
        super(activity);

        sendLogsToServer();
        this.setTitle("App Feedback Sent");
        this.setMessage("Thanks for the feedback, you make our app better!");
        this.setButton(AlertDialog.BUTTON_POSITIVE, "OK,GOT IT", new PositiveButtonClick(activity));
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

    /**
     * Send Logs to Server
     */
    private static void sendLogsToServer(){
        long timeSent = new Date().getTime();
        String appFeedBackSummary = Utility.convertFileToString("AppFeedBackSummary.json");
        if ( appFeedBackSummary.equals("") || appFeedBackSummary.equals("{}") ) {
            return;
        }else{
            try{
                JSONObject appFeedBacksummaryJSON = new JSONObject(appFeedBackSummary);
                JSONArray savedArray = (JSONArray) appFeedBacksummaryJSON.get("saved");
                HashMap<String, String> timeSentMap = new HashMap<>();

                //Add timeSent to all the json file's which are not set with timeSent
                for (int i = 0; i < savedArray.length(); i++) {
                    String instanceName = (String) savedArray.get(i);
                    String screenFeedBackJsonFile = Utility.getJSONfileName(instanceName);
                    String actualTimeSent = Utility.addAndFetchSentTimeFromScreenFeedBackJson(screenFeedBackJsonFile, timeSent);
                    timeSentMap.put(instanceName,actualTimeSent);
                }

                //Iterate each feedback element which is not yet sent
                for (int i = 0; i < savedArray.length(); i++)   {
                    String instanceName = (String)savedArray.get(i);
                    String screenFeedBackJsonFile = Utility.getJSONfileName(instanceName);
                    String actualTimeSent = timeSentMap.get(instanceName);

                    String zipFile = Utility.storageDirectory+instanceName+"_"+ actualTimeSent+".zip";
                    List<String> fileList = new ArrayList<>();
                    fileList.add(Utility.getImageFileName(instanceName));
                    fileList.add(screenFeedBackJsonFile);
                    Utility.createZipArchive(fileList,zipFile);
                    LogPersister.sendInAppFeedBackFile(zipFile, new FeedBackUploadResponseListener(instanceName,zipFile,actualTimeSent));
                }

            }catch (JSONException je){

            }
        }
    }

    private static class FeedBackUploadResponseListener implements ResponseListener {
        private String element;
        private String zipFile;
        private String timeSent;

        public FeedBackUploadResponseListener(String element, String zipFile, String timeSent){
            this.element=element;
            this.zipFile=zipFile;
            this.timeSent=timeSent;
        }

        @Override
        public void onSuccess(Response response) {
            try{
                // regardless of success or failure, reaching this code indicates we successfully communicated with the WL server (we got a reply).

                if(response.getStatus() == 201){
                    Log.i(Utility.LOG_TAG_NAME, "Successfully POSTed feedback data for the instance " + element + ".  HTTP response code: " + response.getStatus());

                    // Thus, we should delete:
                    File zip = new File(zipFile);
                    if(zip.exists()){
                        zip.delete();
                    }
                    Utility.discardFeedbackFiles(element);
                    Utility.updateSummaryJson(element,timeSent);
                }
                else{
                    Log.i(Utility.LOG_TAG_NAME,"Failed to POST feedback data for the file " + element + ". HTTP response code: " + response.getStatus());
                }

            }
            finally{

                // we do this for testing, so unit test code can deterministically stop waiting, and don't have to Thread.sleep
                synchronized(LogPersister.WAIT_LOCK) {
                    LogPersister.WAIT_LOCK.notifyAll ();
                }
            }
        }

        @Override
        public void onFailure(Response response, Throwable t, JSONObject extendedInfo) {

            Log.i(Utility.LOG_TAG_NAME,"External network Access failed. Response: " +response + " JSONObject:" + extendedInfo + " Throwable: " + t );

            // we do this for testing, so unit test code can deterministically stop waiting, and don't have to Thread.sleep
            synchronized(LogPersister.WAIT_LOCK) {
                LogPersister.WAIT_LOCK.notifyAll ();
            }
        }
    }

}
