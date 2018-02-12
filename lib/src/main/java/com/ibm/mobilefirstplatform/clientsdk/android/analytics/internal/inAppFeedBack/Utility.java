package com.ibm.mobilefirstplatform.clientsdk.android.analytics.internal.inAppFeedBack;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.net.Uri;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.widget.ImageView;

import com.ibm.mobilefirstplatform.clientsdk.android.core.api.Response;
import com.ibm.mobilefirstplatform.clientsdk.android.core.api.ResponseListener;
import com.ibm.mobilefirstplatform.clientsdk.android.logger.api.LogPersister;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created by mohlogan on 06/01/18.
 */

public class Utility {

    private static String storageDirectory = null;
    private static String FEEDBACK_STORAGE_LOCATION="/feedback/";
    public static final String LOG_TAG_NAME = Utility.class.getName();

    public static float dipToPixels(Context applicationContext, int dipValue) {
        DisplayMetrics metrics = applicationContext.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, metrics);
    }

    public static float spToPixels(Context applicationContext, float spValue) {
        DisplayMetrics metrics = applicationContext.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, spValue, metrics);
    }

    public static Bitmap getTempBitMap(Context applicationContext, String filename){
        Bitmap bitmap = null;
        try {
            Uri uri = Uri.parse("file://" + Utility.storageDirectory + filename);
            bitmap = BitmapFactory.decodeStream(applicationContext.getContentResolver().openInputStream(uri));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    public static Bitmap getMasterBitMap(Bitmap tempBitmap) {
        Bitmap bitmapMaster = null;
        Bitmap.Config config;
        if(tempBitmap.getConfig() != null){
            config = tempBitmap.getConfig();
        }else{
            config = Bitmap.Config.ARGB_8888;
        }

        //bitmapMaster is Mutable bitmap
        bitmapMaster = Bitmap.createBitmap(
                tempBitmap.getWidth(),
                tempBitmap.getHeight(),
                config);

        return bitmapMaster;
    }

    public static Canvas getCanvas(Bitmap tempBitmap, Bitmap bitmapMaster ){
        Canvas canvasMaster = new Canvas(bitmapMaster);
        canvasMaster.drawBitmap(tempBitmap, 0, 0, null);
        return canvasMaster;
    }

    public static void loadImageFromLocalStore(Context applicationContext, String filename, ImageView imageView) {
        try {
            Uri uri = Uri.parse("file://" + Utility.storageDirectory + filename);
            Bitmap tempBitmap = BitmapFactory.decodeStream(applicationContext.getContentResolver().openInputStream(uri));

            Bitmap.Config config;
            if(tempBitmap.getConfig() != null){
                config = tempBitmap.getConfig();
            }else{
                config = Bitmap.Config.ARGB_8888;
            }

            //bitmapMaster is Mutable bitmap
            Bitmap bitmapMaster = Bitmap.createBitmap(
                    tempBitmap.getWidth(),
                    tempBitmap.getHeight(),
                    config);

            Canvas canvasMaster = new Canvas(bitmapMaster);
            canvasMaster.drawBitmap(tempBitmap, 0, 0, null);

            imageView.setImageBitmap(bitmapMaster);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void saveIamgeToLocalStore(Bitmap finalBitmap, String fileName) {
        File myDir = new File(Utility.storageDirectory);
        myDir.mkdirs();

        if (!myDir.exists()) {
            //TODO: Crash feed back mode
        }

        File file = new File(myDir, fileName);
        if (file.exists()) file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<String> getImageFileList(){
        List<String> list = new ArrayList<>();
        String path = Utility.storageDirectory;
        System.out.println("Path: " + path);

        File directory = new File(path);
        File[] files = directory.listFiles();

        System.out.println("Size: "+ files.length);
        for (int i = 0; i < files.length; i++){
            if(files[i].getName().endsWith(".png")){
                System.out.println("Image FileName:" + files[i].getName());
                list.add(files[i].getName());
            }
        }
        return list;
    }

    /**
     * Return image list which yet to be reviewed. i.e ScreenFeedBack.json doesnt have timeSent set.
     * @return
     */
    public static List<String> getCurrentImageSetForReview(){
        List<String> list = new ArrayList<>();

        String appFeedBackSummary = Utility.convertFileToString("AppFeedBackSummary.json");
        if ( !appFeedBackSummary.equals("") && !appFeedBackSummary.equals("{}") ) {
            try {
                JSONObject appFeedBacksummaryJSON = new JSONObject(appFeedBackSummary);
                JSONArray savedArray = (JSONArray) appFeedBacksummaryJSON.get("saved");

                for (int i = 0; i < savedArray.length(); i++) {
                    String element = (String) savedArray.get(i);
                    String screenFeedBackJsonFile = fetchJSONfileName(element);
                    JSONObject screenFeedBackJson = new JSONObject(Utility.convertFileToString(screenFeedBackJsonFile));
                    try{
                        screenFeedBackJson.get("timeSent");
                    }catch (Exception je1) {
                        list.add(element);
                    }
                }
            } catch (Exception je) {
                //
            }
        }
        return list;
    }

    public static List<String> fetchCommentsFromFile(String commentfile) {
        List<String> list = new ArrayList<>();
        String dir = Utility.storageDirectory;
        File file = new File(dir, commentfile);

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                System.out.println("fetchCommentsFromFile=" + line);
                list.add(line.substring(line.indexOf("=")+1));
            }
            br.close();
        }catch (IOException e) {
            //e.printStackTrace();
        }

        return list;
    }

    public static List<String> fetchCommentsFromJSONFile(String jsonFile) {

        List<String> list = new ArrayList<>();
        String jsonString = convertFileToString(jsonFile);

        if(!jsonString.equals("")){
            try {
                JSONObject obj = new JSONObject(jsonString);
                JSONArray commentArray = (JSONArray) obj.get("comments");
                for(int i = 0; i < commentArray.length(); i++) {
                    list.add((String)commentArray.get(i));
                }
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
        }

        return list;
    }

    public static String convertFileToString(String fileName) {

        String returnStr = "";
        String dir = Utility.storageDirectory;
        File file = new File(dir, fileName);

        if(file.exists()){
            try {
                BufferedReader br = new BufferedReader(new FileReader(file));
                String line;
                while ((line = br.readLine()) != null) {
                    returnStr +=line;
                }
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Log.i(Utility.LOG_TAG_NAME, "convertFileToString: " + file.getName() + ":" +fileName + ":" + returnStr);
        return returnStr;
    }


    public static String fetchCommentfileName(String imageFileName){
        return imageFileName.replace(".png", ".txt");
    }

    public static String fetchJSONfileName(String imageFileName){
        return imageFileName.replace(".png", ".json");
    }

    protected static String getScreenName(String imageFileName){
        return imageFileName.substring(0,imageFileName.indexOf("_"));
    }

    protected static String getTimeCreated(String imageFileName){
        return imageFileName.substring(imageFileName.indexOf("_")+1, imageFileName.lastIndexOf("."));
    }

    protected static String getDeviceID(Context context) {
        String uuid = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);

        return UUID.nameUUIDFromBytes(uuid.getBytes()).toString();
    }

    public static void addDataToFile(String fileName, String data, boolean append){
        String dir = Utility.storageDirectory;
        File file = new File(dir, fileName);
        try {
            FileOutputStream fis = new FileOutputStream(file,append);
            PrintStream ps = new PrintStream(fis);
            ps.print(data+"\n");
            fis.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void deleteAllFiles(){
        String path = Utility.storageDirectory;

        File directory = new File(path);
        File[] files = directory.listFiles();

        System.out.println("Size: "+ files.length);
        for (int i = 0; i < files.length; i++){
            if(files[i].getName().endsWith(".png")){

                //Delete Image file
                if(files[i].exists()){
                    if (!files[i].delete()) {
                        Log.i(Utility.LOG_TAG_NAME, "file could not be deleted :" + files[i].getPath());
                    }
                }

                //Delete Comment File
                String commentFileStr = Utility.fetchCommentfileName(files[i].getName());
                File commentFile = new File(path+"/"+commentFileStr);
                if(commentFile.exists()){
                    if (!commentFile.delete()) {
                        Log.i(Utility.LOG_TAG_NAME, "file could not be deleted :" + commentFile.getPath());
                    }
                }
            }
        }
    }

    public static void discardFeedbackFiles(String filename){
        String path = Utility.storageDirectory;

        //Delete image file
        File file = new File(path,filename);
        if(file.exists()){
            if (!file.delete()) {
                Log.i(Utility.LOG_TAG_NAME, "file could not be deleted :" + file.getPath());
            }
        }

        //Delete Json file if exists
        File jsonFile = new File(path,fetchJSONfileName(filename));
        if(jsonFile.exists()){
            if (!jsonFile.delete()) {
                Log.i(Utility.LOG_TAG_NAME, "file could not be deleted :" + file.getPath());
            }
        }
    }

    public static String generateUniqueImageFileName(String baseName){
        long now = new Date().getTime();
        //String nowString = (String) android.text.format.DateFormat.format("yyyy_MM_dd_hhmmss", now);
        baseName +="_"+now+".png";
        return baseName;
    }

    public static List removeItemFromList(List fileList, String fileEntryToRemove){
        Iterator<String> iterator = fileList.iterator();
        while (iterator.hasNext()) {
            String filename = iterator.next();
            if(filename.equals(fileEntryToRemove)){
                iterator.remove();
                break;
            }
        }
        return fileList;
    }

    public static void setStorageLocation(Context context){
        Utility.storageDirectory = context.getFilesDir().getPath()+"/feedback/";
        Log.i(Utility.LOG_TAG_NAME, "Utility.storageDirectory: "+Utility.storageDirectory);
    }


    private static void createZipArchive(List<String> fileList, String zipFile){
        try {
            int BUFFER = 512;
            BufferedInputStream origin = null;
            FileOutputStream dest = new FileOutputStream(zipFile);
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(
                    dest));
            byte data[] = new byte[BUFFER];

            for (String filename : fileList) {
                filename = Utility.storageDirectory + filename;
                Log.i(Utility.LOG_TAG_NAME, "Compress : Adding: " + filename);
                FileInputStream fi = new FileInputStream(filename);
                origin = new BufferedInputStream(fi, BUFFER);

                ZipEntry entry = new ZipEntry(filename.substring(filename.lastIndexOf("/") + 1));
                out.putNextEntry(entry);
                int count;

                while ((count = origin.read(data, 0, BUFFER)) != -1) {
                    out.write(data, 0, count);
                }
                origin.close();
            }

            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static JSONObject createCommentJSONObject() {
        List<String> fileList = getImageFileList();
        JSONObject commentJSON = new JSONObject();
        try {
            for (final String filename : fileList) {

                JSONArray comment = new JSONArray();
                String commentFileName = fetchCommentfileName(filename);
                File commentFile = new File(Utility.storageDirectory + "/" + commentFileName);

                if (commentFile.exists()) {
                    try {
                        BufferedReader br = new BufferedReader(new FileReader(commentFile));
                        String line;

                        int i =0;
                        while ((line = br.readLine()) != null) {
                            comment.put(line.substring(line.indexOf("=")+1));
                            i++;
                        }
                        br.close();

                    } catch (IOException e) {
                        //nothing to do
                    }
                }
                commentJSON.put(filename, comment);
            }
        }catch (JSONException e) {
            //Nothing to do
        }

        Log.i(Utility.LOG_TAG_NAME, "Comment JSON: " + commentJSON.toString());
        return commentJSON;
    }

    /*protected static void sendLogsToServer1(){

        long now = new Date().getTime();
        final String zipFile = Utility.storageDirectory+"feedback_"+ now+".zip";

        String commentFileName = "CommentFile.json";
        JSONObject commentJSON = createCommentJSONObject();
        List<String> fileList = getImageFileList();

        addDataToFile(commentFileName, commentJSON.toString(), false);
        fileList.add(commentFileName);
        createZipArchive(fileList, zipFile);
        LogPersister.sendInAppFeedBackFile(zipFile, new FeedBackUploadResponseListener());
    }*/

    protected static void sendLogsToServer(){
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
                    String element = (String) savedArray.get(i);
                    String screenFeedBackJsonFile = fetchJSONfileName(element);
                    String actualTimeSent = addAndFetchSentTimeFromScreenFeedBackJson(screenFeedBackJsonFile, timeSent);
                    timeSentMap.put(element,actualTimeSent);
                }

                //Iterate each feedback element which is not yet sent
                for (int i = 0; i < savedArray.length(); i++)   {
                    String element = (String)savedArray.get(i);
                    String screenFeedBackJsonFile = fetchJSONfileName(element);
                    String actualTimeSent = timeSentMap.get(element);

                    String zipFile = Utility.storageDirectory+element+"_"+ actualTimeSent+".zip";
                    List<String> fileList = new ArrayList<>();
                    fileList.add(element);
                    fileList.add(screenFeedBackJsonFile);
                    createZipArchive(fileList,zipFile);
                    LogPersister.sendInAppFeedBackFile(zipFile, new FeedBackUploadResponseListener(element,zipFile,actualTimeSent));
                }

            }catch (JSONException je){

            }
        }
    }

    private static String addAndFetchSentTimeFromScreenFeedBackJson(String jsonFile, long timeSent){
        String screenFeedBackJsonString = Utility.convertFileToString(jsonFile);
        String actualTimeSent = null;
        try{
            JSONObject screenFeedBackJson = new JSONObject(screenFeedBackJsonString);
            try{
                actualTimeSent = (String)screenFeedBackJson.get("timeSent");
            }catch(JSONException js ){
                //do nothing. Exception will be thrown if no timeSent entry present
            }

            //Add timeSent only if not set previously
            if(actualTimeSent==null || actualTimeSent.equals("")) {
                actualTimeSent = ""+timeSent;
                screenFeedBackJson.put("timeSent", actualTimeSent);
                Utility.addDataToFile(jsonFile, screenFeedBackJson.toString(), false);
            }
        }catch(JSONException js ){

        }
        return actualTimeSent;
    }

    private static JSONArray removeEntry(JSONArray inputJsonArray, String entryToRemove){

        JSONArray output = new JSONArray();
        int len = inputJsonArray.length();
        for (int i = 0; i < len; i++)   {

            try {
                String element =  (String)inputJsonArray.get(i);
                if(!element.equals(entryToRemove)){
                    output.put(element);
                }
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
        return output;
    }

    private synchronized static void updateSummaryJson(String sentElement, String timeSent) {
        String appFeedBackSummary = Utility.convertFileToString("AppFeedBackSummary.json");
        Log.i(Utility.LOG_TAG_NAME, "Entering updateSummaryJson: appFeedBackSummary: " + appFeedBackSummary);
        if (appFeedBackSummary.equals("") || appFeedBackSummary.equals("{}")) {
            return;
        } else {
            try {
                JSONObject appFeedBacksummaryJSON = new JSONObject(appFeedBackSummary);
                JSONArray savedArray = (JSONArray) appFeedBacksummaryJSON.get("saved");
                JSONObject sentObject = (JSONObject)appFeedBacksummaryJSON.get("send");

                JSONArray perTimeSentArray = null;
                try{
                    perTimeSentArray = (JSONArray)sentObject.get(timeSent);
                }catch (JSONException je){
                    //do nothing. Exception will be thrown if no Array present for given timeSent
                }

                if(perTimeSentArray!=null){
                    perTimeSentArray.put(sentElement);
                }else{
                    perTimeSentArray = new JSONArray();
                    perTimeSentArray.put(sentElement);
                }

                savedArray = removeEntry(savedArray,sentElement);
                sentObject.put(timeSent,perTimeSentArray);

                appFeedBacksummaryJSON.put("saved",savedArray);
                appFeedBacksummaryJSON.put("send", sentObject);
                Log.i(Utility.LOG_TAG_NAME, "appFeedBackSummary: " + appFeedBacksummaryJSON.toString());
                Utility.addDataToFile("AppFeedBackSummary.json", appFeedBacksummaryJSON.toString(), false);
            }catch (JSONException je){
                je.printStackTrace();
            }
        }
    }

    static class FeedBackUploadResponseListener implements ResponseListener {
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
                    Log.i(Utility.LOG_TAG_NAME, "Successfully POSTed feedback data for the file " + element + ".  HTTP response code: " + response.getStatus());

                    // Thus, we should delete:
                    File zip = new File(zipFile);
                    if(zip.exists()){
                        zip.delete();
                    }
                    Utility.discardFeedbackFiles(element);
                    updateSummaryJson(element,timeSent);
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
