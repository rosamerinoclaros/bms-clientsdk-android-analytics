package com.ibm.mobilefirstplatform.clientsdk.android.analytics.internal.inAppFeedBack;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.icu.util.Freezable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ibm.mobilefirstplatform.clientsdk.android.R;

import java.util.HashMap;
import java.util.List;

/**
 * Created by mohlogan on 04/01/18.
 */

public class ReviewPopup extends Activity {
    //private ImageView imageView;

    List<String> fileList = null;
    static FrameLayout currentEnabledButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.review_popup);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout(width,height);

        Bundle extras = getIntent().getExtras();
        String fileNameFromIntentBundle = null;
        if(extras != null){
            fileNameFromIntentBundle = extras.getString("imagename");
        }

        int sp20 = (int)Utility.spToPixels(this,20);
        int sp50 = (int)Utility.spToPixels(this,50);
        int sp60 = (int)Utility.spToPixels(this,60);
        int dp2 = (int)Utility.dipToPixels(this, 2);
        int dp10 = (int)Utility.dipToPixels(this, 10);
        int dp5 = (int)Utility.dipToPixels(this, 5);

        final LinearLayout imageButtonlayout = (LinearLayout)findViewById(R.id.image_button_view);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(sp60,sp60);
        params.setMargins(0,0,dp10,0);
        //params.setMargins(dp5,dp5,dp5,dp5);

        //fileList = Utility.getImageFileList();
        fileList = Utility.getCurrentImageSetForReview();

        //if(fileList.size()==0){
        //    finish();
        //}

        boolean isImageViewSet = false;

        final HashMap<String, Object> frameLayoutMap = new HashMap<>();
        currentEnabledButton = null;

        for(final String filename : fileList){

            final FrameLayout frameLayout = new FrameLayout(this);
            frameLayout.setLayoutParams(params);
            frameLayout.setBackgroundColor(Color.parseColor("#E8EAF6"));

            FrameLayout.LayoutParams param1 = new FrameLayout.LayoutParams(sp20,sp20);
            params.setMargins(0,0,dp5,0);


            //Close button at the top left cornor of the iconised screenshot button
            final Button closebutton = new Button(this);
            closebutton.setText("x");
            closebutton.setTextColor(Color.parseColor("#000000"));
            closebutton.setTextSize((int)Utility.spToPixels(this,3));
            closebutton.setTypeface(Typeface.DEFAULT_BOLD);
            closebutton.setBackgroundResource(R.drawable.sqaure_button_background);
            closebutton.setGravity(Gravity.LEFT|Gravity.TOP);
            //closebutton.setBackgroundColor(Color.parseColor("#FF0000"));
            closebutton.setLayoutParams(param1);
            closebutton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Utility.discardFeedbackFiles(filename);
                    imageButtonlayout.removeView(frameLayout);
                    fileList = Utility.removeItemFromList(fileList, filename);

                    //Remove the current view and show first image in the imageView
                    if(fileList.size()>0){
                        String filename = fileList.get(0);
                        FrameLayout currentFrameLayout = (FrameLayout)frameLayoutMap.get(filename);
                        currentFrameLayout.setBackgroundColor(Color.parseColor("#90CAF9"));
                        showEditedImageWithComment(filename);
                        currentEnabledButton = currentFrameLayout;
                    }else{
                        LinearLayout linearLayout = (LinearLayout)findViewById(R.id.relative_view);
                        linearLayout.removeAllViews();
                    }
                }
            });

            //Image Button shows iconised view of screenshot
            final ImageView imageButton = new ImageButton(this);
            imageButton.setLayoutParams(params);
            imageButton.setScaleType(ImageView.ScaleType.FIT_CENTER);
            imageButton.setAdjustViewBounds(true);
            imageButton.setScaleType(ImageView.ScaleType.FIT_XY);
            Utility.loadImageFromLocalStore(this, filename, imageButton);
            frameLayout.addView(imageButton);

            frameLayout.addView(closebutton);
            imageButtonlayout.addView(frameLayout);
            frameLayoutMap.put(filename, frameLayout);

            imageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(currentEnabledButton!=null){
                        currentEnabledButton.setBackgroundColor(Color.parseColor("#E8EAF6"));
                    }
                    frameLayout.setBackgroundColor(Color.parseColor("#90CAF9"));
                    currentEnabledButton = frameLayout;
                    showEditedImageWithComment(filename);
                }
            });

            if(filename.equals(fileNameFromIntentBundle)){
                if(currentEnabledButton!=null){
                    currentEnabledButton.setBackgroundColor(Color.parseColor("#E8EAF6"));
                }
                frameLayout.setBackgroundColor(Color.parseColor("#90CAF9"));
                currentEnabledButton = frameLayout;
                showEditedImageWithComment(filename);
                isImageViewSet = true;
            }
        }

        if(!isImageViewSet){
            int size = fileList.size();
            if(size>0){
                String filename = fileList.get(size-1);
                if(currentEnabledButton!=null){
                    currentEnabledButton.setBackgroundColor(Color.parseColor("#E8EAF6"));
                }

                FrameLayout currentFrameLayout = (FrameLayout)frameLayoutMap.get(filename);
                currentFrameLayout.setBackgroundColor(Color.parseColor("#90CAF9"));
                showEditedImageWithComment(filename);
                currentEnabledButton = currentFrameLayout;
            }
        }

        //Plus Button
        Button button = new Button(this);
        button.setText("+");
        button.setTextColor(Color.parseColor("#000000"));
        button.setTextSize(Utility.spToPixels(this,10));
        button.setTypeface(Typeface.DEFAULT_BOLD);
        button.setBackgroundResource(R.drawable.sqaure_button_background);
        LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(sp50,sp50);
        params2.setMargins(0,0,dp10,0);
        button.setLayoutParams(params2);
        imageButtonlayout.addView(button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Button submitButton = (Button) findViewById(R.id.submitButton);
        submitButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                //Utility.deleteAllFiles();
                new SubmitAppFeedBack(ReviewPopup.this).show();
            }
        });
    }

    private void showEditedImageWithComment(String filename){
        LinearLayout linearLayout = (LinearLayout)findViewById(R.id.relative_view);
        linearLayout.removeAllViews();


        ImageView imageView = new ImageView(this);
        imageView.setTag(filename);
        imageView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        imageView.getLayoutParams().height = (int)(getWindowManager().getDefaultDisplay().getHeight() * 0.70);
        imageView.setBackgroundColor(Color.parseColor("#FDC3A2"));
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        Utility.loadImageFromLocalStore(this, filename, imageView);

        //for (int i=0; i < 2; i++){
            Toast toast = Toast.makeText(this, "Scroll Down To View the Comments", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.TOP|Gravity.CENTER, 3, 10);
            toast.show();
        //}

        linearLayout.addView(imageView);
        linearLayout.addView(drawLine());

        int i=0;
        for (String comment : Utility.fetchCommentsFromJSONFile(Utility.fetchJSONfileName(filename))) {
            i++;
            System.out.println("value= " + comment);

            TextView commentNameLable = new TextView(this);
            commentNameLable.setText("\nComment #"+i);
            commentNameLable.setBackgroundColor(Color.parseColor("#F6E9E2"));
            commentNameLable.setTypeface(Typeface.DEFAULT_BOLD);

            TextView commentTextLable = new TextView(this);
            commentTextLable.setText(comment+"\n");
            commentTextLable.setBackgroundColor(Color.parseColor("#F6E9E2"));
            commentNameLable.setTypeface(Typeface.SANS_SERIF);

            linearLayout.addView(commentNameLable);
            linearLayout.addView(commentTextLable);
            linearLayout.addView(drawLine());
        }
    }

    public void closeActivity(View v) {
        new DismissAppFeedBack(this, "ALL", true).show();
    }

    private View drawLine(){
        View line = new View(this);
        line.setBackgroundColor(Color.BLACK);
        line.setMinimumWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        line.setMinimumHeight((int)Utility.dipToPixels(this,1));
        return line;
    }
}
