

package com.links.gaurav.lnotes;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;

import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.widget.Button;

import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.github.fafaldo.fabtoolbar.widget.FABToolbarLayout;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import data.ImageProperties;
import utilities.BitmapFunctions;
import utilities.FocusIndicatorView;
import utilities.ImageProcessor;

import static utilities.PointMapFunctions.MapToPoint;

public class OpenCamera extends AppCompatActivity implements SurfaceHolder.Callback,Camera.PictureCallback, Camera.PreviewCallback{
    private MediaPlayer _shootMP = null;
    SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    private List<Size> mSupportedPreviewSizes;
    ImageButton capture;
    ImageButton gallery;
    ImageButton mode;
    Camera mCamera;
    private boolean attemptToFocus = false;
    private FABToolbarLayout fabToolbarLayout;
    SharedPreferences mSharedPref;
    Boolean mFocused;
    Boolean mFlashMode;
    Boolean mBugRotate;
    Boolean MultiMode;
    float mDist;
    public PointF[] Result_Points;
    private Matrix matrix;
    private static final int REQ_FOR_SAVING=5;
    private int Request_for_Points=111;
    public static int Req_for_Marker=2,Req_for_Image=1;


    int total;
    Vector<String>  AllCapturedImages=new Vector<String>();
    Vector<String>  previewImages=new Vector<String>();
    Boolean safeToTakePicture;
    Boolean tmove;
    private OpenCamera mthis;
    public  Vector<ImageProperties> imageProperties=new Vector<>();
    FloatingActionButton fabToolbarButton;
    //public ImageProperties imagePropertiess[];
    SlideShowDialogFragment newFragment = SlideShowDialogFragment.newInstance();
    private FocusIndicatorView mFocusIndicaor;
    private int option;



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        mthis=this;
        setContentView(R.layout.camera);
        option=(int)getIntent().getExtras().get("option");
        matrix= new Matrix();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        init();



    }


    private void init(){

        total=0;
        mSharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        mFlashMode=false;
        MultiMode=false;
        tmove=true;
        mode=findViewById(R.id.multiShot);
        capture=findViewById(R.id.Capture_Image);
        gallery=findViewById(R.id.galleryview);
        gallery.setVisibility(View.GONE);
        Result_Points=new PointF[20];
        turnCameraOn();
        fabToolbarLayout=(FABToolbarLayout) findViewById(R.id.fabtoolbar);
        fabToolbarButton = (FloatingActionButton) findViewById(R.id.fabtoolbar_fab);
        fabToolbarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fabToolbarLayout.show();
            }
        });
        final ImageView flashModeButton = (ImageView) findViewById(R.id.flash_button);
        flashModeButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mFlashMode = setFlash(!mFlashMode);
                ((ImageView)v).setColorFilter(mFlashMode ? 0xFFFFFFFF : 0x00000000);

            }
        });
        final ImageView hideButton = (ImageView) findViewById(R.id.hide_button);
        hideButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                fabToolbarLayout.hide();
            }
        });
        if(option==Req_for_Marker)
        {
            mode.setVisibility(View.GONE);
        }


        mFocusIndicaor=findViewById(R.id.af_indicator);
        mFocusIndicaor.showStart();
        mFocusIndicaor.setVisibility(View.GONE);

        OpenCVLoader.initDebug();

    }
    public void turnCameraOn() {

        mSurfaceView = findViewById(R.id.surfaceView);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(this);
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mSurfaceView.setVisibility(SurfaceView.VISIBLE);
        final GestureDetector gestureDetector=new GestureDetector(OpenCamera.this,new GestureDetector.SimpleOnGestureListener(){

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                return true;
            }
        });
        mSurfaceView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                Camera.Parameters params = mCamera.getParameters();
                int action = event.getAction();
                if (action == MotionEvent.ACTION_DOWN) {
                        mDist = event.getY();
                    }
                if (action == MotionEvent.ACTION_UP) {

                    if (tmove) {
                        handleFocus(event, params);
                    } else tmove = true;
                    return true;
                }

                if (action == MotionEvent.ACTION_MOVE) {
                    if (mDist!=event.getY()&&params.isZoomSupported()) {
                            mCamera.cancelAutoFocus();
                            handleZoom(event, params);
                            tmove = false;
                        }
                    return true;
                }
                    // handle single touch events
                return true;
            }

        });
        capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestPicture();
            }
        });
        mode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ToggleMode();
            }
        });
        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //open a gallery..
                if(total>0)
                    StartFragment(previewImages.toArray(new String[total]),0,total,REQ_FOR_SAVING);
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode==Request_for_Points&&resultCode==RESULT_OK) {
            imageProperties.remove(newFragment.currentPosition);
            previewImages.set(newFragment.currentPosition,data.getExtras().getString("preview"));
            imageProperties.add((newFragment.currentPosition),(ImageProperties)data.getExtras().get("imageProperty"));
            newFragment.Update(previewImages.toArray(new String[previewImages.size()]));
        }
    }


    protected void ToggleMode(){
        if(total<=1){
        if(MultiMode){

            mode.setAlpha(.4f);
            MultiMode=false;
            gallery.setVisibility(View.GONE);
        }else {
            mode.setAlpha(1f);
            MultiMode=true;
            gallery.setVisibility(View.VISIBLE);
            }
        }

    }

    private void handleZoom(MotionEvent event, Camera.Parameters params) {
        int maxZoom = params.getMaxZoom();
        int zoom = params.getZoom();
        float newDist = event.getY();
        if (newDist < mDist) {
            // zoom in
            if (zoom < maxZoom)
                zoom+=2;
        } else if (newDist > mDist) {
            // zoom out
            if (zoom > 0)
                zoom-=2;
        }
        mDist = newDist;
        params.setZoom(zoom);
        mCamera.setParameters(params);
        attemptToFocus=false;
        safeToTakePicture=true;

    }

    public void handleFocus(MotionEvent event, Camera.Parameters parameters) {
        attemptToFocus=true;
        safeToTakePicture = false;

        mCamera.cancelAutoFocus();
        try {
            mFocusIndicaor.animate().cancel();
            mFocusIndicaor.animate().setListener(null);
        }catch (Exception e){

        }

        mFocusIndicaor.clearAnimation();
        mFocusIndicaor.setVisibility(View.VISIBLE);
        mFocusIndicaor.setAlpha(1);


        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(((int)event.getX() - 25),((int) event.getY() - 25), 0, 0);
        mFocusIndicaor.setTranslationX(event.getX() - 25);
        mFocusIndicaor.setTranslationY(event.getY()-25);
        mFocusIndicaor.animate().alpha(0.0f).setDuration(5000).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mFocusIndicaor.setVisibility(View.GONE);
            }
        });
        Rect focusRect = calculateTapArea(mSurfaceView,event.getX(), event.getY(), 1f);
        Rect meteringRect = calculateTapArea(mSurfaceView,event.getX(), event.getY(), 1.5f);
       // Toast.makeText(OpenCamera.this,event.getX()+" "+event.getY()+","+mSurfaceView.getWidth()+" "+mSurfaceView.getHeight(),Toast.LENGTH_SHORT).show();


        // check if parameters are set (handle RuntimeException: getParameters failed (empty parameters))
        if (parameters != null) {
            if(parameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_AUTO))
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            List<Camera.Area> areas=new ArrayList<>();
            areas.add(new Camera.Area(focusRect, 1000));
            parameters.setFocusAreas(areas);

            if (parameters.getMaxNumMeteringAreas()>0) {
                areas=new ArrayList<>();
                areas.add(new Camera.Area(meteringRect, 1000));
                parameters.setMeteringAreas(areas);
            }

            try {
                mCamera.setParameters(parameters);

            } catch (Exception e) {

            }
        }
        mCamera.autoFocus(new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean b, Camera camera) {
                attemptToFocus=false;
                safeToTakePicture=true;
            }
        });
        mCamera.setAutoFocusMoveCallback(new Camera.AutoFocusMoveCallback() {
            @Override
            public void onAutoFocusMoving(boolean b, Camera camera) {

            }
        });

    }
    public  Rect calculateTapArea(View v, float x1, float y1, float coefficient) {
        float x =mSurfaceView.getWidth()-(mSurfaceView.getHeight()-y1)*mSurfaceView.getWidth()/mSurfaceView.getHeight() ;
        float y =mSurfaceView.getHeight()- x1* mSurfaceView.getHeight()/ mSurfaceView.getWidth();

        float focusAreaSize = 50;

        int areaSize = Float.valueOf(focusAreaSize * coefficient).intValue();
        int centerX = (int) (x / v.getWidth() * 2000 - 1000);
        int centerY = (int) (y / v.getHeight() * 2000 - 1000);

        int left = clamp(centerX - areaSize / 2, -1000, 1000);
        int right = clamp(left + areaSize, -1000, 1000);
        int top = clamp(centerY - areaSize / 2, -1000, 1000);
        int bottom = clamp(top + areaSize, -1000, 1000);


        return new Rect(left, top, right, bottom);
    }
    private int clamp(int x, int min, int max) {
        if (x > max) {
            return max;
        }
        if (x < min) {
            return min;
        }
        return x;
    }
    public List<android.hardware.Camera.Size> getResolutionList() {
        return mCamera.getParameters().getSupportedPreviewSizes();

    }

    public List<Camera.Size> getPictureResolutionList() {
        return mCamera.getParameters().getSupportedPictureSizes();
    }
    public Camera.Size getMaxPreviewResolution() {
        int maxWidth=0;
        Camera.Size curRes=null;

        mCamera.lock();

        for ( Camera.Size r: getResolutionList() ) {
            if (r.width>maxWidth) {
                maxWidth=r.width;
                curRes=r;
            }
        }

        return curRes;
    }
    public Camera.Size getMaxPictureResolution(float previewRatio) {
        int maxPixels=0;
        int ratioMaxPixels=0;
        Camera.Size currentMaxRes=null;
        Camera.Size ratioCurrentMaxRes=null;
        for ( Camera.Size r: getPictureResolutionList() ) {
            if(option==Req_for_Marker){
                if(r.width<=800||r.height<=800){
                float pictureRatio = (float) r.width / r.height;
                int resolutionPixels = r.width * r.height;

                if (resolutionPixels>ratioMaxPixels && pictureRatio == previewRatio) {
                    ratioMaxPixels=resolutionPixels;
                    ratioCurrentMaxRes=r;
                }

                if (resolutionPixels>maxPixels) {
                    maxPixels=resolutionPixels;
                    currentMaxRes=r;
                }
                }

            }else{
            float pictureRatio = (float) r.width / r.height;
            int resolutionPixels = r.width * r.height;

            if (resolutionPixels>ratioMaxPixels && pictureRatio == previewRatio) {
                ratioMaxPixels=resolutionPixels;
                ratioCurrentMaxRes=r;
            }

            if (resolutionPixels>maxPixels) {
                maxPixels=resolutionPixels;
                currentMaxRes=r;
            }}
        }

        boolean matchAspect = mSharedPref.getBoolean("match_aspect", true);

        if (ratioCurrentMaxRes!=null && matchAspect) {
            return ratioCurrentMaxRes;
        }

        return currentMaxRes;
    }
    private int findBestCamera() {
        int cameraId = -1;
        //Search for the back facing camera
        //get the number of cameras
        int numberOfCameras = Camera.getNumberOfCameras();
        //for every camera check
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                cameraId = i;
                break;
            }
            cameraId = i;
        }
        return cameraId;
    }



    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {

        try {
            int cameraId = findBestCamera();
            mCamera = Camera.open(cameraId);
        }

        catch (RuntimeException e) {
            return;
        }

        Camera.Parameters param;
        param = mCamera.getParameters();

        Camera.Size pSize = getMaxPreviewResolution();
        param.setPreviewSize(pSize.width, pSize.height);

        float previewRatio = (float) pSize.width / pSize.height;

        Display display = getWindowManager().getDefaultDisplay();
        android.graphics.Point size = new android.graphics.Point();
        display.getRealSize(size);

        int displayWidth = Math.min(size.y, size.x);
        int displayHeight = Math.max(size.y, size.x);

        float displayRatio =  (float) displayHeight / displayWidth;

        int previewHeight = displayHeight;

        if ( displayRatio > previewRatio ) {
            ViewGroup.LayoutParams surfaceParams = mSurfaceView.getLayoutParams();
            previewHeight = (int) ( (float) size.y/displayRatio*previewRatio);
            surfaceParams.height = previewHeight;
            mSurfaceView.setLayoutParams(surfaceParams);


        }

        int hotAreaWidth = displayWidth / 4;
        int hotAreaHeight = previewHeight / 2 - hotAreaWidth;

        Camera.Size maxRes = getMaxPictureResolution(previewRatio);
        if ( maxRes != null) {
            param.setPictureSize(maxRes.width, maxRes.height);
        }

        PackageManager pm = getPackageManager();
        if (pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_AUTOFOCUS)) {
            param.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);

        } else {
            mFocused = true;

        }
        if (pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            param.setFlashMode(mFlashMode ? Camera.Parameters.FLASH_MODE_ON : Camera.Parameters.FLASH_MODE_OFF);
        }
        mSupportedPreviewSizes=getResolutionList();
        param.setRotation(90);
        param.setJpegQuality(75);
        mCamera.setParameters(param);

        mBugRotate = mSharedPref.getBoolean("bug_rotate", false);

        if (mBugRotate) {
            mCamera.setDisplayOrientation(270);
        } else {
            mCamera.setDisplayOrientation(90);
        }
        try {
            mCamera.setAutoFocusMoveCallback(new Camera.AutoFocusMoveCallback() {
                @Override
                public void onAutoFocusMoving(boolean start, Camera camera) {
                    mFocused = !start;

                }
            });
        } catch (Exception e) {
        }

        // some devices doesn't call the AutoFocusMoveCallback - fake the
        // focus to true at the start

        mFocused = true;
        safeToTakePicture = true;
    }



    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int width, int height) {
        refreshCamera();
        Matrix matrix = new Matrix();

        matrix.postScale(width / 2000f, height / 2000f);
        matrix.postTranslate(width / 2f, height / 2f);
        matrix.invert(this.matrix);
    }
    private void refreshCamera() {

        try {

            mCamera.stopPreview();
        }

        catch (Exception e) {
        }

        try {
            mCamera.setPreviewDisplay(mSurfaceHolder);
            mCamera.startPreview();
            mCamera.setPreviewCallbackWithBuffer(this);
        }
        catch (Exception e) {
        }
        safeToTakePicture=true;
        attemptToFocus=false;
    }
    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
        }

    }
    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {

    }

    public boolean requestPicture() {

        if (safeToTakePicture&&!(newFragment!=null &&  newFragment.getDialog()!=null
                && newFragment.getDialog().isShowing())) {
            if(!safeToTakePicture)
                return true;
            safeToTakePicture = false;
            mCamera.cancelAutoFocus();
            mCamera.takePicture(null,null,mthis);
            return true;
        }
        return false;
    }

    @Override
    public void onPictureTaken(byte[] bytes, Camera camera) {
        shootSound();
        String path;
        android.hardware.Camera.Size pictureSize = camera.getParameters().getPictureSize();
        path=Save();

        FileOutputStream fos = null;
        File file=new File(path);
        try {

            fos = new FileOutputStream(file);
            // Writes bytes from the specified byte array to this file output stream
            try {
                fos.write(bytes);
            }catch (IOException e){}
        }
        catch (FileNotFoundException e) {}
        bytes=null;
        int width=pictureSize.width,height=pictureSize.height;

        if(height<width)
        {
            int t=width;
            width=height;
            height=t;
        }
        Map<Integer, PointF> outlinePoints = new HashMap<>();
        outlinePoints.put(0, new PointF(0, 0));
        outlinePoints.put(1, new PointF(width, 0));
        outlinePoints.put(2, new PointF(0,height));
        outlinePoints.put(3, new PointF(width, height));

        try{
            if(option==Req_for_Marker){
                Intent intent=new Intent();
                intent.putExtra("path",path);
                intent.putExtra("height",height);
                intent.putExtra("width",width);
                setResult(RESULT_OK,intent);
                finish();
            }
            else {

                   if(MultiMode){
                    if(total<=20) {
                        mode.setVisibility(View.GONE);
                        AllCapturedImages.add(path);
                        previewImages.add(CreatePreviewImages(path));
                        imageProperties.add(total,new ImageProperties(path,MapToPoint(outlinePoints),  Modify.ScanMode.values()[Integer.parseInt(mSharedPref.getString(SettingsFragment.KEY_PREF_SCANNINGMODE,"3"))]));
                        total++;
                    } else {Toast.makeText(this,"No More Than 20 Images plzzz",Toast.LENGTH_SHORT).show();
                    }
                }
                else {//Delete UnWanted Files here Later
                        AllCapturedImages.clear();
                        previewImages.clear();
                        AllCapturedImages.add(path);
                        imageProperties.clear();
                        previewImages.add(CreatePreviewImages(path));
                        imageProperties.add(new ImageProperties(path,MapToPoint(outlinePoints), Modify.ScanMode.values()[Integer.parseInt(mSharedPref.getString(SettingsFragment.KEY_PREF_SCANNINGMODE,"3"))]));
                        total=1;
                        if(previewImages.toArray().length!=0)
                        StartFragment(previewImages.toArray(new String[total]),0,total,REQ_FOR_SAVING);
                    }
            }
        }catch (NullPointerException n){
            setResult(RESULT_CANCELED);
            finish();
        }

        refreshCamera();
        safeToTakePicture = true;
    }
    public String Save(){
        try {File image = File.createTempFile(
                "Orig",  /* prefix */
                ".jpg",         /* suffix */
                getExternalFilesDir("Pictures")      /* directory */
        );
            return image.getAbsolutePath();
        } catch (IOException ex) {
            // Error occurred while creating the File
        }
        return null;
    }


    private void shootSound()
    {
        AudioManager meng = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        int volume = meng.getStreamVolume(AudioManager.STREAM_NOTIFICATION);

        if (volume != 0)
        {
            if (_shootMP == null) {
                _shootMP = MediaPlayer.create(this, Uri.parse("file:///system/media/audio/ui/camera_click.ogg"));
            }
            if (_shootMP != null) {
                _shootMP.start();
            }
        }
    }

    public void results_from_camera()
    {newFragment.onStop();
        Intent intent=new Intent();
        intent.putExtra("total",total);
        intent.putExtra("imageProperties",imageProperties.toArray(new ImageProperties[total]));
        intent.putExtra("PreviewImages",previewImages.toArray(new String[total]));
        setResult(RESULT_OK,intent);
        finish();

    }
    public boolean setFlash(boolean stateFlash) {
        PackageManager pm = getPackageManager();
        if (pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            Camera.Parameters par = mCamera.getParameters();
            if(par.getSupportedFlashModes().contains(Camera.Parameters.FLASH_MODE_ON))
            {
                par.setFlashMode(stateFlash ? Camera.Parameters.FLASH_MODE_ON : Camera.Parameters.FLASH_MODE_OFF);
                mCamera.setParameters(par);
            }else {
                par.setFlashMode(stateFlash ? Camera.Parameters.FLASH_MODE_TORCH : Camera.Parameters.FLASH_MODE_OFF);
                mCamera.setParameters(par);
            }
            return stateFlash;
        }
        return false;
    }
    public void Modify_Image(int position){
        Intent intent=new Intent(this,Modify.class);
        intent.putExtra("path",imageProperties.get(position).getOriginal_Image());
        startActivityForResult(intent,Request_for_Points);

    }
    public void StartFragment(String[] path,int position,int total,int code){
        FragmentTransaction ft=getSupportFragmentManager().beginTransaction();
        Bundle bundle=new Bundle();
        bundle.putStringArray("path",path);
        bundle.putInt("position",position);
        bundle.putInt("total",total);
        bundle.putInt("Request_Code",code);
        newFragment.setArguments(bundle);
        newFragment.show(ft, "slideshow");
    }
    public String CreatePreviewImages(String s){
        try{
            File file=File.createTempFile("tmp",".jpg",getExternalFilesDir("TEMP"));
            int Screenwidth = Resources.getSystem().getDisplayMetrics().widthPixels;
            Bitmap bmp= BitmapFunctions.decodeSampledBitmapFromResource(s,Screenwidth/2,Screenwidth/2);
            Mat tmat=new Mat(bmp.getHeight(),bmp.getWidth(), CvType.CV_8UC4);
            Utils.bitmapToMat(bmp,tmat);
            bmp.recycle();
            Imgproc.cvtColor(tmat,tmat,Imgproc.COLOR_RGBA2BGRA);
            ImageProcessor.ProcessImage(tmat, Modify.ScanMode.values()[Integer.parseInt(mSharedPref.getString(SettingsFragment.KEY_PREF_SCANNINGMODE,"3"))],2,9,1);
            Imgcodecs.imwrite(file.getAbsolutePath(),tmat);
            return  file.getAbsolutePath();
        }catch (Exception e){
                Log.e("mapp","m",e);
        }
        return  null;
    }
    public void tost(String x){
        Toast.makeText(this,x,Toast.LENGTH_SHORT).show();
    }

}
