package com.ibm.mobilefirstplatform.clientsdk.android.analytics.internal.inaAppFeedBack;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

/**
 * Created by mohlogan on 04/01/18.
 */

public class ReviewPopup extends Activity {
    //private ImageView imageView;

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
        String fileNameFromIntentBundle = extras.getString("imagename");

        int sp20 = (int)Utility.spToPixels(this,20);
        int sp50 = (int)Utility.spToPixels(this,50);
        int sp60 = (int)Utility.spToPixels(this,60);
        int dp2 = (int)Utility.dipToPixels(this, 2);
        int dp5 = (int)Utility.dipToPixels(this, 10);
        int dp10 = (int)Utility.dipToPixels(this, 5);

        final LinearLayout imageButtonlayout = (LinearLayout)findViewById(R.id.image_button_view);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(sp60,sp60);
        params.setMargins(0,0,dp10,0);
        //params.setMargins(dp5,dp5,dp5,dp5);

        String defaultFile = "";
        for(final String filename : Utility.getImageFileList("temp")){

            final FrameLayout frameLayout = new FrameLayout(this);
            frameLayout.setLayoutParams(params);

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

            defaultFile=filename;
            imageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showEditedImageWithComment(filename);
                }
            });
        }

        if(fileNameFromIntentBundle != "") {
            showEditedImageWithComment(fileNameFromIntentBundle);
        }else {
            showEditedImageWithComment(defaultFile);
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
                Utility.deleteAllFiles();
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

        linearLayout.addView(imageView);
        linearLayout.addView(drawLine());

        int i=0;
        for (String comment : Utility.fetchCommentsFromFile(Utility.fetchCommentfileName(filename))) {
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
        //new DismissAppFeedBack(this).show();
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
