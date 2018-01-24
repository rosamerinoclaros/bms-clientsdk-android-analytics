package com.ibm.mobilefirstplatform.clientsdk.android.analytics.internal.inAppFeedback;

import android.graphics.Bitmap;
import android.view.View;

/**
 * Created by mohlogan on 27/12/17.
 */

public class ScreenShot {
    public static Bitmap takeScreenShot(View v){
        v.setDrawingCacheEnabled(true);
        v.buildDrawingCache(true);
        Bitmap b = Bitmap.createBitmap(v.getDrawingCache());
        v.setDrawingCacheEnabled(false);
        return b;
    }

    public static Bitmap takeScreenShotOfRootView(View v){
        return takeScreenShot(v.getRootView().getRootView());
    }
}
