package com.links.gaurav.lnotes;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.Request;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import data.ImageProperties;

/**
 * Created by Gaurav on 7/25/2017.
 */

public class Crop extends Activity {
    private Button scanButton;
    private ImageView sourceImageView;
    private FrameLayout sourceFrame;
    private PolygonView polygonView;
    private View view;
    private ProgressDialog progressDialogFragment;
    String path,tempPath;
    float rotation=0;

    private Bitmap original;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!OpenCVLoader.initDebug()) {
            // Handle initialization error
            Toast.makeText(Crop.this,"Error",Toast.LENGTH_SHORT).show();
        }
        else {
            setContentView(R.layout.crop_layout);
            sourceImageView = (ImageView) findViewById(R.id.sourceImageView);

            scanButton = (Button) findViewById(R.id.scanButton);
            //scanButton.setOnClickListener(new ScanButtonClickListener()); //Create a view.onclk listnr and process a=in AsyncTask xoxo
            sourceFrame = (FrameLayout) findViewById(R.id.sourceFrame);
            polygonView = (PolygonView) findViewById(R.id.polygonView);
            sourceFrame.post(new Runnable() {
                @Override
                public void run() {
                    original = getBitmap();
                    if (original != null) {
                        setBitmap(original);
                    }
                }
            });


            if (savedInstanceState == null) {
                Bundle extras = getIntent().getExtras();
                if (extras == null) {
                    path = null;
                } else {
                    path = extras.getString("path");
                    rotation=extras.getFloat("rotation",0);
                }
            } else {
                path = (String) savedInstanceState.getSerializable("path");
            }

                            //setLayout();
            scanButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(polygonView.getPoints().size()==4) {

                       /* //CODE FOR CROPPING.... IMPORTANT ..DONT YOU DELETE
                        Mat doc = CropfourPoints.fourPointTransform(Imgcodecs.imread(path, Imgcodecs.IMREAD_UNCHANGED), SetPointToRatio(MapToPoint(polygonView.getPoints())));
                        try {
                            File temp=createImageFile();
                            Imgproc.cvtColor(doc,doc,Imgproc.COLOR_RGBA2GRAY);
                            Imgproc.adaptiveThreshold(doc, doc, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 15, 15);
                        Imgcodecs.imwrite(tempPath, doc);
                            Toast.makeText(Crop.this,doc.width()+"x"+doc.height(),Toast.LENGTH_SHORT).show();
                        doc.release();
                            polygonView.setVisibility(View.GONE);
                            Intent intent = new Intent(Crop.this,FullscreenImage.class);
                            intent.putExtra("path", tempPath);
                            Crop.this.startActivity(intent);
                        }catch (IOException e){}*/

                        Intent intent=new Intent();
                        Map<Integer,PointF> map=polygonView.getPoints();
                        intent.putExtra("Point1",(PointF)map.get(0));
                        intent.putExtra("Point2",(PointF)map.get(1));
                        intent.putExtra("Point3",(PointF)map.get(2));
                        intent.putExtra("Point4",(PointF)map.get(3));
                        intent.putExtra("Ratio",getRatio());
                        setResult(RESULT_OK,intent);
                        finish();
                    }else Toast.makeText(Crop.this,"Error",Toast.LENGTH_SHORT).show();

                }
            });

        }

    }





    private Bitmap getBitmap() {
        try {
            Bitmap bitmap=null;
            File f= new File(path);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            bitmap = BitmapFactory.decodeStream(new FileInputStream(f), null, options);
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    private void setBitmap(Bitmap original) {
        Bitmap scaledBitmap = scaledBitmap(original, sourceFrame.getWidth(), sourceFrame.getHeight());
        sourceImageView.setImageBitmap(scaledBitmap);
        Bitmap tempBitmap = ((BitmapDrawable) sourceImageView.getDrawable()).getBitmap();

        polygonView.setVisibility(View.VISIBLE);
        final int padding = (int) getResources().getDimension(R.dimen.scanPadding);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(tempBitmap.getWidth() + 2 * padding, tempBitmap.getHeight() + 2 * padding);
        layoutParams.gravity = Gravity.CENTER;
        sourceImageView.post(new Runnable() {
            @Override
            public void run() {
                Map<Integer, PointF> pointFs = getOutlinePoints(sourceImageView);
                polygonView.setPoints(pointFs);
                FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams((sourceImageView.getWidth() + 2 * padding), (sourceImageView.getHeight() + 2 * padding));
                layoutParams.gravity = Gravity.CENTER;
                //Toast.makeText(getApplicationContext(),(sourceImageView.getWidth() + 2 * padding)+" "+(sourceImageView.getHeight() + 2 * padding),Toast.LENGTH_SHORT).show();
                polygonView.setLayoutParams(layoutParams);
            }
        });
                polygonView.setLayoutParams(layoutParams);
    }
    private Bitmap scaledBitmap(Bitmap bitmap, int width, int height) {
        Matrix m = new Matrix();

        m.setRectToRect(new RectF(0, 0, bitmap.getWidth(), bitmap.getHeight()), new RectF(0, 0, width, height), Matrix.ScaleToFit.CENTER);
        m.postRotate(rotation);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),bitmap.getHeight(), m, true);

    }


    private Map<Integer, PointF> getOutlinePoints(ImageView tempBitmap) {

        Map<Integer, PointF> outlinePoints = new HashMap<>();
        outlinePoints.put(0, new PointF(0, 0));
        outlinePoints.put(1, new PointF(tempBitmap.getWidth(), 0));
        outlinePoints.put(2, new PointF(0, tempBitmap.getHeight()));
        outlinePoints.put(3, new PointF(tempBitmap.getWidth(), tempBitmap.getHeight()));

        return outlinePoints;
    }

    private double getRatio(){
        double ratio=rotation/90%2==0?((double)original.getHeight())/sourceImageView.getHeight():((double)original.getHeight())/sourceImageView.getWidth();
        return ratio;
    }







}

