/*
 *     Copyright 2015 IBM Corp.
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */

package com.ibm.mobilefirstplatform.clientsdk.android.analytics.internal;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.content.Context;
import android.support.v4.content.ContextCompat;
import com.ibm.mobilefirstplatform.clientsdk.android.logger.api.LogPersister;
import com.ibm.mobilefirstplatform.clientsdk.android.logger.api.Logger;
import android.support.annotation.RequiresPermission;

public class MFPAnalyticsLocationListener implements LocationListener {

    protected static Logger logger = Logger.getLogger(LogPersister.INTERNAL_PREFIX + MFPAnalyticsLocationListener.class.getSimpleName());
    private static MFPAnalyticsLocationListener instance = null;

    private int TIME_DELAY = 1000 * 60 * 2; //2 min

    private boolean initLocationRequests = false;

    private static Context Context = null;

    private static LocationManager manager = null;
    private Location bestLocation = null;

    private Location oldLocation = null;

    public static MFPAnalyticsLocationListener getInstance(Context context){
        if(instance == null) {
            instance = new MFPAnalyticsLocationListener();
            Context = context;
            manager = (LocationManager)context.getSystemService(context.LOCATION_SERVICE);
        }


        return instance;
    }

    @RequiresPermission(anyOf = {android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION})
    public void init(){

        if(isNetworkEnabled() && checkPermission()){
            initLocationRequests = true;
            logger.info("Init network request");
            manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, instance);
        }

    }

    @RequiresPermission(anyOf = {android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION})
    public void unregister(){
        if(checkPermission() && instance != null){
            manager.removeUpdates(instance);
        }
    }


    @Override
    public void onLocationChanged(Location location) {
        if(oldLocation == null){
            oldLocation = location;
        } else {
            if(isBetterLocation(oldLocation, location)){
                bestLocation = location;
            } else {
                bestLocation = oldLocation;
            }
        }

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }


    public boolean isNetworkEnabled() {
        return manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    @RequiresPermission(anyOf = {android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION})
    public double getLatitude(){
        if(checkPermission() && bestLocation == null && initLocationRequests){
            return manager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER).getLatitude();
        } else if(!checkPermission() || !initLocationRequests){
            throw new SecurityException("Check to see if your location permissions have been enabled or called the init method");
        }

        return bestLocation.getLatitude();
    }

    @RequiresPermission(anyOf = {android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION})
    public double getLongitude() throws SecurityException{
        if(checkPermission() && bestLocation == null){
            return manager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER).getLongitude();
        } else if(!checkPermission() && bestLocation == null){
            throw new SecurityException("Check to see if your location permissions have been enabled");
        }

        return bestLocation.getLongitude();
    }

    private boolean isBetterLocation(Location oldLocation, Location newLocation){
        if(oldLocation == null){
            return true;
        }

        // Check if the new location is newer than old location
        long delta = newLocation.getTime() - oldLocation.getTime();
        boolean isReallyNew = delta > TIME_DELAY;
        boolean isReallyOld = delta < -TIME_DELAY;
        boolean isNew = delta > 0;

        // If it greater than TIME_DELAY than the user must have moved and vice versa
        if(isReallyNew){
            return true;
        } else if (isReallyOld){
            return false;
        }

        //Check accuracy (the higher the number the lower the accuracy)
        int accuracy = (int) (newLocation.getAccuracy() - oldLocation.getAccur
                \   
        } else if(isNew && !isVeryInaccurate){
            return true;
        }

        return false;
    }

    private boolean checkPermission() {
        return Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission(Context, android.Manifest.permission.ACCESS_FINE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(Context, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED;
    }
}


