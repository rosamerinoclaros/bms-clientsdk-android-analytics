package com.ibm.mobilefirstplatform.clientsdk.android.analytics.internal.inAppFeedBack;

import android.graphics.Bitmap;
import android.view.View;

/**
 * Created by mohlogan on 27/12/17.
 */

public class ScreenShot1 {
    public static Bitmap takeScreenShot1(View v){
        v.setDrawingCacheEnabled(true);
        v.buildDrawingCache(true);
        Bitmap b = Bitmap.createBitmap(v.getDrawingCache());
        v.setDrawingCacheEnabled(false);
        return b;
    }

    public static Bitmap takeScreenShotOfRootView1(View v){
        return takeScreenShot1(v.getRootView().getRootView());
    }
}
