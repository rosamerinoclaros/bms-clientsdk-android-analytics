/*
    Copyright 2015 IBM Corp.
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
        http://www.apache.org/licenses/LICENSE-2.0
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/
package com.ibm.mobilefirstplatform.clientsdk.android.logger.internal;


import com.ibm.mobilefirstplatform.clientsdk.android.core.api.ResponseListener;
import com.ibm.mobilefirstplatform.clientsdk.android.logger.api.LogPersister;
import com.ibm.mobilefirstplatform.clientsdk.android.logger.api.Logger;

import org.json.JSONObject;

public class LogPersisterDelegate implements LogPersisterInterface {

    @Override
    public void setLogLevel(Logger.LEVEL level) {
        LogPersister.setLogLevel(level);
    }

    @Override
    public Logger.LEVEL getLogLevel() {
        return LogPersister.getLogLevel();
    }

    @Override
    public Logger.LEVEL getLogLevelSync() {
        return LogPersister.getLevelSync();
    }

    @Override
    public void storeLogs(boolean shouldStoreLogs) {
        LogPersister.storeLogs(shouldStoreLogs);
    }

    @Override
    public boolean isStoringLogs() {
        return LogPersister.getCapture();
    }

    @Override
    public void setMaxLogStoreSize(int bytes) {
        LogPersister.setMaxLogStoreSize(bytes);
    }

    @Override
    public int getMaxLogStoreSize() {
        return LogPersister.getMaxLogStoreSize();
    }

    @Override
    public void send(ResponseListener responseListener) {
        LogPersister.send(responseListener);
    }

    @Override
    public boolean isUncaughtExceptionDetected() {
        return LogPersister.isUnCaughtExceptionDetected();
    }

    @Override
    public void doLog(Logger.LEVEL level, String message, long timestamp, Throwable throwable, JSONObject jsonObject, Logger logger) {
        LogPersister.doLog(level, message, timestamp, throwable, jsonObject, logger.getName(), Logger.isInternalLogger(logger), logger);
    }
}
