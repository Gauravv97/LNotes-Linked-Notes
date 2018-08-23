package com.links.gaurav.lnotes;

import android.animation.Animator;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Process;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;

import data.ImageProperties;
import utilities.BitmapFunctions;
import utilities.PointMapFunctions;
import utilities.ImageProcessor;

import static utilities.PointMapFunctions.MapToPoint;

/**
 * Created by Gaurav on 12/25/2017.
 */

public class Modify extends Activity {
    ImageView imageView;
    LinearLayout tuningLayout;
    LinearLayout modeLayout;
    LinearLayout ModeOriginal,ModeGray,ModeColor,ModeBnW1,ModeBnW2;
    boolean tuningLayout_togg=false;
    boolean modeLayout_togg=false;
    String image_path;
    int h=0,w=0;
    boolean safeRotate=true;
    Bitmap imageBitmap,tmp;
    Mat omat,cmat,tmat;
    double ratioIbyO;
    private double colorGain = 2;       // contrast
    private final int Request_for_Points=112;
    private ImageProperties imageProperties;
    static int orignal_imageHeight,original_imageWidth;
    int details=9;
    Set_ImageViewTask set_imageViewTask;
    public enum ScanMode{ORIGINAL,GRAYSCALE,COLOR_SCAN,BLACK_AND_WHITE1,BLACK_AND_WHITE2}
    SharedPreferences sharedPref;
    ScanMode currentMode;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.modify_image);
        imageView=(ImageView)findViewById(R.id.imageView_modify);
        tuningLayout=(LinearLayout)findViewById(R.id.tunelayout_modify);
        modeLayout=(LinearLayout)findViewById(R.id.modeLayout_modify);
        ImageView tuningButton=(ImageView)findViewById(R.id.tune_image_modify);
        ImageView cropButton=(ImageView)findViewById(R.id.crop_image_modify);
        ImageView saveButton=(ImageView)findViewById(R.id.save_image_modify);
        ImageView rotateButton=(ImageView)findViewById(R.id.rotate_image_modify);
        ImageView modeButton=(ImageView)findViewById(R.id.mode_modify);
        SeekBar param2seek=(SeekBar)findViewById(R.id.seekBar2);
        set_imageViewTask=new Set_ImageViewTask();
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        currentMode=ScanMode.values()[Integer.parseInt(sharedPref.getString(SettingsFragment.KEY_PREF_SCANNINGMODE,"3"))];

        modeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(tuningLayout_togg)
                    toggle_tuningLayout();
                toggle_modeLayout();
            }
        });
        tuningButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(modeLayout_togg)
                    toggle_modeLayout();
                toggle_tuningLayout();
            }
        });
        cropButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(safeRotate){
                    Intent intent=new Intent(Modify.this,Crop.class);
                    intent.putExtra("path",image_path);
                    intent.putExtra("rotation",imageView.getRotation());
                    Modify.this.startActivityForResult(intent,Request_for_Points);
                }
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent=new Intent();
                imageProperties.setContrast(colorGain);
                imageProperties.setDetails(details);
                imageProperties.setScanMode(currentMode);

                if(currentMode==ScanMode.ORIGINAL||currentMode==ScanMode.COLOR_SCAN)
                    Imgproc.cvtColor(tmat, tmat, Imgproc.COLOR_RGBA2BGRA);

                imageProperties.setRotation(imageView.getRotation());
                int rotflag=((int)imageView.getRotation()/90)%4;
                if (rotflag == 1){
                    Core.rotate(tmat,tmat,Core.ROTATE_90_CLOCKWISE);
                } else if (rotflag == 2) {
                    Core.rotate(tmat,tmat,Core.ROTATE_180);

                } else if (rotflag ==3){
                    Core.rotate(tmat,tmat,Core.ROTATE_90_COUNTERCLOCKWISE);
                }
                try{
                    File file=File.createTempFile("tmp",".jpg",getExternalFilesDir("TEMP"));
                    Imgcodecs.imwrite(file.getAbsolutePath(),tmat);
                    intent.putExtra("preview",file.getAbsolutePath());
                }catch (Exception e){Log.e("hh","nn",e);
                }
                intent.putExtra("imageProperty",imageProperties);
                setResult(RESULT_OK,intent);
                finish();

            }
        });


        rotateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(imageView.getRotation()/90%2==0){
                    h=imageView.getHeight();
                    w=imageView.getWidth();

                }

                if(safeRotate)
                {
                    imageView.animate().rotationBy(90).scaleX(tmp.getHeight()>tmp.getWidth()?(imageView.getRotation()/90%2==0?(w*1.0f/h):1):1).scaleY(tmp.getHeight()>tmp.getWidth()?(imageView.getRotation()/90%2==0?(w*1.0f/h):1):1).setDuration(150).setInterpolator(new LinearInterpolator()).setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        safeRotate=false;
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        safeRotate=true;
                    }


                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                }).start();}
            }
        });

        param2seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                details=1+(9-i)*2;
                ((TextView)findViewById(R.id.detailsText)).setText("Details:"+i);
                if(set_imageViewTask.getStatus()==AsyncTask.Status.RUNNING) set_imageViewTask.cancel(true);
                set_imageViewTask=new Set_ImageViewTask();
                set_imageViewTask.execute();

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        ((SeekBar)findViewById(R.id.contrastSeekbar)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                colorGain=1.5+i*.1;
                ((TextView)findViewById(R.id.contrastText)).setText("Contrast:"+i);
                if(set_imageViewTask.getStatus()==AsyncTask.Status.RUNNING) set_imageViewTask.cancel(true);
                set_imageViewTask=new Set_ImageViewTask();
                set_imageViewTask.execute();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        if(!OpenCVLoader.initDebug())
            this.finish();
        image_path=getIntent().getExtras().getString("path");
        int Screenwidth = Resources.getSystem().getDisplayMetrics().widthPixels;
        imageBitmap= BitmapFunctions.decodeSampledBitmapFromResource(image_path,Screenwidth/2,Screenwidth/2);
        original_imageWidth=BitmapFunctions.getHeightWidthOfImage(image_path).second;
        orignal_imageHeight=BitmapFunctions.getHeightWidthOfImage(image_path).first;
        Map<Integer, PointF> outlinePoints = new HashMap<>();
        outlinePoints.put(0, new PointF(0, 0));
        outlinePoints.put(1, new PointF(original_imageWidth, 0));
        outlinePoints.put(2, new PointF(0,orignal_imageHeight));
        outlinePoints.put(3, new PointF(original_imageWidth, orignal_imageHeight));
        imageProperties=new ImageProperties(image_path,MapToPoint(outlinePoints),currentMode);
        ratioIbyO=imageBitmap.getHeight()*1.0/orignal_imageHeight;
        omat=new Mat(imageBitmap.getHeight(),imageBitmap.getWidth(),CvType.CV_8UC4);
        Utils.bitmapToMat(imageBitmap,omat);
        imageBitmap.recycle();
        cmat=omat.clone();
        imageView.setImageBitmap(tmp);
        ModeOriginal=findViewById(R.id.OriginalMode_modify);
        ModeOriginal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SetMode(ScanMode.ORIGINAL);
            }
        });
        ModeColor=findViewById(R.id.ColorMode_modify);
        ModeColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SetMode(ScanMode.COLOR_SCAN);
            }
        });
        ModeGray=findViewById(R.id.GrayscaleMode_modify);
        ModeGray.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SetMode(ScanMode.GRAYSCALE);
            }
        });
        ModeBnW1=findViewById(R.id.BnW1Mode_modify);
        ModeBnW1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SetMode(ScanMode.BLACK_AND_WHITE1);
            }
        });
        ModeBnW2= findViewById(R.id.BnW2Mode_modify);
        ModeBnW2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SetMode(ScanMode.BLACK_AND_WHITE2);
            }
        });
        SetMode(currentMode);
    }
    private void SetMode(ScanMode mode){
        currentMode=mode;
        ModeOriginal.setAlpha(.4f);
        ModeColor.setAlpha(.4f);
        ModeGray.setAlpha(.4f);
        ModeBnW1.setAlpha(.4f);
        ModeBnW2.setAlpha(.4f);
        switch (mode){
            case BLACK_AND_WHITE2:ModeBnW2.setAlpha(1);break;
            case ORIGINAL: ModeOriginal.setAlpha(1);break;
            case BLACK_AND_WHITE1:ModeBnW1.setAlpha(1);break;
            case COLOR_SCAN: ModeColor.setAlpha(1);break;
            case GRAYSCALE:ModeGray.setAlpha(1);break;
        }
        if(set_imageViewTask.getStatus()==AsyncTask.Status.RUNNING) set_imageViewTask.cancel(true);
        set_imageViewTask=new Set_ImageViewTask();
        set_imageViewTask.execute();

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode==Request_for_Points&&resultCode==RESULT_OK) {

            Map<Integer, PointF> map = new HashMap<>();
            map.put(0, (PointF) data.getExtras().get("Point1"));
            map.put(1, (PointF) data.getExtras().get("Point2"));
            map.put(2, (PointF) data.getExtras().get("Point3"));
            map.put(3, (PointF) data.getExtras().get("Point4"));
            imageProperties.setPoints(PointMapFunctions.SetPointToRatio(MapToPoint(map), (Double) data.getExtras().get("Ratio")));

           for(int i=((int)(imageView.getRotation()/90))%4;i>0;i--) {
               if(i%2==0)
                   imageProperties.rotatePoints(orignal_imageHeight,original_imageWidth);
               else imageProperties.rotatePoints(original_imageWidth,orignal_imageHeight);
           }
            cmat=CropfourPoints.fourPointTransform(omat, PointMapFunctions.SetPointToRatio(imageProperties.getPoints(),ratioIbyO));
            set_ImageView();
            imageView.setImageBitmap(tmp);
        }
    }
    public void set_ImageView(){
        tmat=cmat.clone();
        ImageProcessor.ProcessImage(tmat,currentMode,colorGain,details,1);
        tmp=Bitmap.createBitmap(tmat.width(),tmat.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(tmat,tmp);
    }




    private void toggle_modeLayout(){

        if(modeLayout_togg){
            modeLayout_togg=false;
            slideDown(modeLayout);
        }else {
            modeLayout_togg=true;
            slideUp(modeLayout);
        }
    }
    private void toggle_tuningLayout(){

        if(tuningLayout_togg){
            tuningLayout_togg=false;
            slideDown(tuningLayout);
        }else {
            tuningLayout_togg=true;
            slideUp(tuningLayout);
        }
    }
    public void slideUp(View view){
        view.setVisibility(View.VISIBLE);
        TranslateAnimation animate = new TranslateAnimation(
                0,                 // fromXDelta
                0,                 // toXDelta
                view.getHeight(),  // fromYDelta
                0);                // toYDelta
        animate.setDuration(500);
        view.startAnimation(animate);
    }

    // slide the view from its current position to below itself
    public void slideDown(View view){
        TranslateAnimation animate = new TranslateAnimation(
                0,                 // fromXDelta
                0,                 // toXDelta
                0,                 // fromYDelta
                view.getHeight()); // toYDelta
        animate.setDuration(500);
        view.startAnimation(animate);
        view.setVisibility(View.GONE);
    }
    private class Set_ImageViewTask extends AsyncTask<Void,Void,Void>{
        public Set_ImageViewTask(){

        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            imageView.setImageBitmap(tmp);
        }

        @Override
        protected Void doInBackground(Void... voids) {

            Process.setThreadPriority(Process.THREAD_PRIORITY_FOREGROUND);
            set_ImageView();
            return null;
        }
    }
}
