package com.ibm.mobilefirstplatform.clientsdk.android.analytics.internal.inAppFeedback;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.ibm.mobilefirstplatform.clientsdk.android.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mohlogan on 27/12/17.
 */
public class EditorPopup extends Activity{

    private ImageView imageView;
    private ImageButton drawButton;
    private ImageButton eraseButton;
    private ImageButton commentButton;
    private EditText editText;
    private TextView commentTextLable;
    private View editGroup;
    private static String filename;

    Bitmap bitmapMaster;
    Canvas canvasMaster;

    int prvX, prvY;
    Paint paintDraw;
    Paint paintBlur;
    private static boolean drawToggle=false;
    private static boolean eraseToggle=false;
    private static boolean commentToggle=false;
    private static boolean allowComment;
    private static boolean fileSaved;
    private static int count;

    public static List<String> commentList;
    private static boolean isEdited = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.popupwindow);

        count = 0;
        commentList = new ArrayList<>();
        allowComment=true;
        isEdited = false;
        fileSaved = false;

        imageView = (ImageView) findViewById(R.id.imageView);
        drawButton = (ImageButton) findViewById(R.id.drawButton);
        eraseButton = (ImageButton) findViewById(R.id.eraseButton);
        commentButton = (ImageButton) findViewById(R.id.commentButton);
        editText = (EditText) findViewById(R.id.edit_text);
        commentTextLable = (TextView) findViewById(R.id.comment_text);
        editGroup = findViewById(R.id.textLayout);

        editGroup.setVisibility(View.GONE);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        //getWindow().setLayout((int)(width*0.9),(int)(height*0.9));
        getWindow().setLayout(width,height);

        imageView.getLayoutParams().height = (int)(getWindowManager().getDefaultDisplay().getHeight() * 0.90);
        imageView.getLayoutParams().width = (int)(getWindowManager().getDefaultDisplay().getWidth() * 0.95);

        Bundle extras = getIntent().getExtras();
        filename = extras.getString("imagename");
        loadImageFromLocalStore(filename);

        paintDraw = new Paint();
        paintDraw.setStyle(Paint.Style.FILL);
        paintDraw.setColor(Color.parseColor("#FF9052"));
        paintDraw.setStrokeWidth(10);

        paintBlur = new Paint();
        paintBlur.setStyle(Paint.Style.FILL);
        paintBlur.setColor(Color.parseColor("#7998a8"));
        paintBlur.setStrokeWidth(50);

        editText.setOnEditorActionListener(new TextView.OnEditorActionListener(){
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_GO) {
                    InputMethodManager imm = (InputMethodManager)v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    String comment = editText.getText().toString();
                    commentList.add(comment);

                    allowComment=true;
                    editGroup.setVisibility(View.GONE);
                    handled = true;
                }
                return handled;
            }
        });

        Button reviewButton= (Button) findViewById(R.id.reviewButton);
        reviewButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //Save file only if something edited on image
                if(isEdited){
                    saveImageAndComment();
                }else{
                    Utility.discardFeedbackFiles(filename);
                    filename = "";
                }
                //new DismissAppFeedBack(this, filename, isEdited, "review").show();
                Intent intent = new Intent(EditorPopup.this, ReviewPopup.class);
                intent.putExtra("imagename", filename);
                startActivityForResult(intent, 200);
            }
        });

        drawButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                //Change the icon
                if(!drawToggle){
                    drawButton.setImageResource(R.drawable.draw_yellow);
                    drawToggle=true;
                    eraseButton.setImageResource(R.drawable.erase_black);
                    eraseToggle=false;
                    commentButton.setImageResource(R.drawable.comment_black);
                    commentToggle=false;
                }else{
                    drawButton.setImageResource(R.drawable.draw_black);
                    drawToggle=false;
                }
            }
        });

        eraseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                //Change the icon
                if(!eraseToggle){
                    eraseButton.setImageResource(R.drawable.erase_yellow);
                    eraseToggle=true;
                    drawButton.setImageResource(R.drawable.draw_black);
                    drawToggle=false;
                    commentButton.setImageResource(R.drawable.comment_black);
                    commentToggle=false;
                }else{
                    eraseButton.setImageResource(R.drawable.erase_black);
                    eraseToggle=false;
                }
            }
        });

        commentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                //Change the icon
                if(!commentToggle){
                    commentButton.setImageResource(R.drawable.comment_yellow);
                    commentToggle=true;
                    drawButton.setImageResource(R.drawable.draw_black);
                    drawToggle=false;
                    eraseButton.setImageResource(R.drawable.erase_black);
                    eraseToggle=false;
                }else{
                    commentButton.setImageResource(R.drawable.comment_black);
                    commentToggle=false;
                }
            }
        });

        imageView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if(drawToggle || eraseToggle || commentToggle ){
                    int action = event.getAction();
                    int x = (int) event.getX();
                    int y = (int) event.getY();
                    switch (action) {
                        case MotionEvent.ACTION_DOWN:
                            prvX = x;
                            prvY = y;
                            drawOnScreenshot((ImageView) v, bitmapMaster, prvX, prvY, x, y);
                            break;
                        case MotionEvent.ACTION_MOVE:
                            if(!commentToggle) drawOnScreenshot((ImageView) v, bitmapMaster, prvX, prvY, x, y);
                            prvX = x;
                            prvY = y;
                            break;
                        case MotionEvent.ACTION_UP:
                            if(!commentToggle) drawOnScreenshot((ImageView) v, bitmapMaster, prvX, prvY, x, y);
                            break;
                    }
                }
                return true;
            }
        });
    }

    private void saveImageAndComment(){
        if(!fileSaved){
            Utility.saveIamgeToLocalStore(bitmapMaster, filename);
            int i = 0;
            for(String comment: commentList){
                Utility.appendToFile(Utility.fetchCommentfileName(filename), "comment"+i+"="+comment);
                i++;
            }
            fileSaved=true;
        }
    }

    private void drawOnScreenshot(ImageView iv, Bitmap bm,
                                  float x0, float y0, float x, float y){
        if(x<0 || y<0 || x > iv.getWidth() || y > iv.getHeight()){
            //outside ImageView
            return;
        }else{
            isEdited = true;
            float ratioWidth = (float)bm.getWidth()/(float)iv.getWidth();
            float ratioHeight = (float)bm.getHeight()/(float)iv.getHeight();

            if(commentToggle && allowComment){
                allowComment=false;

                Paint pCircle = new Paint();
                pCircle.setColor(Color.parseColor("#FF9052"));
                canvasMaster.drawCircle(x * ratioWidth, y * ratioHeight,  Utility.dipToPixels(getApplicationContext(),20), pCircle);

                count +=1;
                String drawText = ""+(count);
                Paint paint = new Paint();
                paint.setColor(Color.BLACK);
                paint.setTextSize(Utility.dipToPixels(getApplicationContext(),20));
                canvasMaster.drawText(drawText, x * ratioWidth, y * ratioHeight, paint);

                //Enable edittext
                commentTextLable.setText("Comment #"+count);
                editGroup.setVisibility(View.VISIBLE);
                editText.setText("");
            }else{
                canvasMaster.drawLine(
                        x0 * ratioWidth,
                        y0 * ratioHeight,
                        x * ratioWidth,
                        y * ratioHeight,
                        ((eraseToggle)? paintBlur: paintDraw));
            }
            imageView.invalidate();
        }
    }

    private void loadImageFromLocalStore(String filename){
        Bitmap tempBitmap = Utility.getTempBitMap(this,filename);
        bitmapMaster = Utility.getMasterBitMap(tempBitmap);
        canvasMaster = Utility.getCanvas(tempBitmap,bitmapMaster);
        imageView.setImageBitmap(bitmapMaster);
    }

    public void closeActivity(View v) {
        if(isEdited){
            saveImageAndComment();
        }
        new DismissAppFeedBack(this, filename, isEdited).show();
    }

    // Storage Permissions variables
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    //persmission method.
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have read or write permission
        int writePermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int readPermission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);

        if (writePermission != PackageManager.PERMISSION_GRANTED || readPermission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==200) {
            finish();
        }
    }
}
