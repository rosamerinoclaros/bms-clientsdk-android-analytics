# bms-clientsdk-android-analytics

[![Build Status](https://travis-ci.org/ibm-bluemix-mobile-services/bms-clientsdk-android-analytics.svg?branch=master)](https://travis-ci.org/ibm-bluemix-mobile-services/bms-clientsdk-android-analytics)
[![Build Status](https://travis-ci.org/ibm-bluemix-mobile-services/bms-clientsdk-android-analytics.svg?branch=development)](https://travis-ci.org/ibm-bluemix-mobile-services/bms-clientsdk-android-analytics)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.ibm.mobilefirstplatform.clientsdk.android/analytics/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.ibm.mobilefirstplatform.clientsdk.android/analytics)

##Release Notes:

###1.1.6
- Fixed issue where developers would get a 403 when sending logs/analytics to the Mobile Analytics service.

###1.1.5:
- Added the ability to record network transactions by using `Analytics.DeviceEvent.NETWORK` as one of the device events when calling `Analytics.initialize()`.

###1.1.4:
- Fixed issue regarding an exception log that was being shown when reading the SDK version number.

###1.1.3:
- Fixed NullPointerException when calling `Analytics.send()` or `Logger.send()` without a response listener.

###1.1.2:
- Fixed problem with `Analytics.init()` where if you set `hasUserContext` to `false`, it would not properly initialize.

###1.1.1:
- Changed Javadoc and corrected small logic error

###1.1.0:
- Added new initializer in order to avoid double counting anonymous users and named users as the same. Deprecated `BMSAnalytics.clearUserIdentity()`, will be removed in 2.x.

###1.0.5:
- Android Nougat officially supported; changed target SDK version to Android 24.

###1.0.3:
- Fixed error with App Sessions not being properly counted.

Known limitation - currently v2.x of Android SDK does not submit monitoring data. This is a work in progress and will be delivered in following months. If you’d like to continue receiving monitoring data in the service dashboard you can continue using the v1.x SDK.

Copyright 2016 IBM Corp.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
