package com.ibm.mobilefirstplatform.clientsdk.android.analytics.internal.inAppFeedback;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by mohlogan on 06/01/18.
 */

public class Utility {

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
            Uri uri = Uri.parse("file://" + Environment.getExternalStorageDirectory().toString() + "/temp/"+ filename);
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
            Uri uri = Uri.parse("file://" + Environment.getExternalStorageDirectory().toString() + "/temp/"+ filename);
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
        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/temp");
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

    public static List<String> getImageFileList(String dir){
        List<String> list = new ArrayList<>();
        String path = Environment.getExternalStorageDirectory().toString()+"/"+dir;
        System.out.println("Path: " + path);

        File directory = new File(path);
        File[] files = directory.listFiles();

        System.out.println("Size: "+ files.length);
        for (int i = 0; i < files.length; i++){
            if(files[i].getName().endsWith(".png")){
                System.out.println("ImageeqFileName:" + files[i].getName());
                list.add(files[i].getName());
            }
        }
        return list;
    }

    public static List<String> fetchCommentsFromFile(String commentfile) {
        List<String> list = new ArrayList<>();
        String dir = Environment.getExternalStorageDirectory().toString()+"/temp/";
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
            e.printStackTrace();
        }

        return list;
    }

    public static String fetchCommentfileName(String imageFileName){
        return imageFileName.replace(".png", ".txt");
    }

    public static void appendToFile(String commentFileName, String data){
        String dir = Environment.getExternalStorageDirectory().toString()+"/temp/";
        File file = new File(dir, commentFileName);
        try {
            FileOutputStream fis = new FileOutputStream(file,true);
            PrintStream ps = new PrintStream(fis);
            ps.print(data+"\n");
            fis.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void deleteAllFiles(){
        String path = Environment.getExternalStorageDirectory().toString()+"/temp/";

        File directory = new File(path);
        File[] files = directory.listFiles();

        System.out.println("Size: "+ files.length);
        for (int i = 0; i < files.length; i++){
            if(files[i].getName().endsWith(".png")){

                //Delete Image file
                if(files[i].exists()){
                    if (!files[i].delete()) {
                        System.out.println("file could not be deleted :" + files[i].getPath());
                    }
                }

                //Delete Comment File
                String commentFileStr = Utility.fetchCommentfileName(files[i].getName());
                File commentFile = new File(path+"/"+commentFileStr);
                if(commentFile.exists()){
                    if (!commentFile.delete()) {
                        System.out.println("file could not be deleted :" + commentFile.getPath());
                    }
                }
            }
        }
    }

    public static void discardFeedbackFiles(String filename){
        String path = Environment.getExternalStorageDirectory().toString()+"/temp";
        File file = new File(path,filename);
        if(file.exists()){
            if (file.delete()) {
                System.out.println("file could not be deleted :" + file.getPath());
            }
        }
    }

    public static String generateUniqueImageFileName(){
        String filename = "fb";
        Date now = new Date();
        String nowString = (String) android.text.format.DateFormat.format("yyyy_MM_dd_hhmmss", now);
        filename +="_"+nowString+".png";
        return filename;
    }
}
