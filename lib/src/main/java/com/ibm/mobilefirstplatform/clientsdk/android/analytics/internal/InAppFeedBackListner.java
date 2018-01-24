package com.ibm.mobilefirstplatform.clientsdk.android.analytics.internal;

import com.ibm.mobilefirstplatform.clientsdk.android.analytics.internal.inAppFeedback.*;
import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.support.v4.app.ActivityCompat;

/**
 * Created by mohlogan on 16/01/18.
 */

public class InAppFeedBackListner {

    private static InAppFeedBackListner instance = null;
    private static Context applicationContext = null;

    // Storage Permissions variables
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    //persmission method.
    private void verifyStoragePermissions(Activity activity) {
        // Check if we have read or write permission
        int writePermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int readPermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);

        if (writePermission != PackageManager.PERMISSION_GRANTED || readPermission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    public static InAppFeedBackListner getInstance(Context context) {

        applicationContext = context;

        if (instance == null) {
            instance = new InAppFeedBackListner();
        }

        return instance;
    }

    public void triggerFeedbackMode(){
        Activity activity = (Activity) applicationContext;

        verifyStoragePermissions(activity);

        Bitmap image = ScreenShot.takeScreenShotOfRootView(activity.getWindow().getDecorView().getRootView());
        String filename = Utility.generateUniqueImageFileName();
        Utility.saveIamgeToLocalStore(image, filename);

        Intent intent = new Intent(activity, EditorPopup.class);
        intent.putExtra("imagename", filename);
        activity.startActivity(intent);
    }

    public Activity getActivity(Context context){
        return (Activity) context;

        /*if(context == null){
            return null;
        }else if((context instanceof ContextWrapper) && (context instanceof Activity)){
            return (Activity) context;
        }else if(context instanceof ContextWrapper){
            return getActivity(((ContextWrapper) context).getBaseContext());
        }

        return null;*/
    }
}
