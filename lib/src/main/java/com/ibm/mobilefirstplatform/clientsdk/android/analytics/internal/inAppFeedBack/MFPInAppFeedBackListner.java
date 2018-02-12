package com.ibm.mobilefirstplatform.clientsdk.android.analytics.internal.inAppFeedBack;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;

/**
 * Created by mohlogan on 16/01/18.
 */

public class MFPInAppFeedBackListner {

    private static Handler handler;
    private static Runnable runnable;

    private static MFPInAppFeedBackListner instance = null;
    private static Context applicationContext = null;

    private static Context context = null;

    public static void setContext(Context context){
        if(null == MFPInAppFeedBackListner.context) {
            MFPInAppFeedBackListner.context = context;
            Utility.setStorageLocation(MFPInAppFeedBackListner.context);
        }
    }

    public static Context getContext(){
        return MFPInAppFeedBackListner.context;
    }

    // Storage Permissions variables
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };


    private static boolean hasStoragePermissions() {
        int writePermission = ActivityCompat.checkSelfPermission(MFPInAppFeedBackListner.context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int readPermission = ActivityCompat.checkSelfPermission(MFPInAppFeedBackListner.context, Manifest.permission.READ_EXTERNAL_STORAGE);

        if (writePermission == PackageManager.PERMISSION_GRANTED && readPermission == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }

    //persmission method.
    private static void requestStoragePermissions(Activity activity) {
        // Check if we have read or write permission
        int writePermission = ActivityCompat.checkSelfPermission(MFPInAppFeedBackListner.context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int readPermission = ActivityCompat.checkSelfPermission(MFPInAppFeedBackListner.context, Manifest.permission.READ_EXTERNAL_STORAGE);

        if (writePermission != PackageManager.PERMISSION_GRANTED || readPermission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    public static void triggerFeedbackMode(final Activity activity){
        if(null != MFPInAppFeedBackListner.context) {

            handler = new Handler();
            runnable = new Runnable() {
                @Override
                public void run() {

                    Bitmap image = ScreenShot.takeScreenShotOfRootView(activity.getWindow().getDecorView().getRootView());
                    String filename = Utility.generateUniqueImageFileName(activity.getClass().getName());
                    System.out.println("UniqueFileName:" + filename);
                    Utility.saveIamgeToLocalStore(image, filename);

                    Intent intent = new Intent(activity, EditorPopup.class);
                    intent.putExtra("imagename", filename);
                    activity.startActivity(intent);
                }
            };

            if(!hasStoragePermissions()) {
                requestStoragePermissions(activity);
            }
            handler.postDelayed(runnable, 1500);
        }
    }
}
