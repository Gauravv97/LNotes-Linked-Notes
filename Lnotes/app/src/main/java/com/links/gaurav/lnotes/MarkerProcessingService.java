package com.links.gaurav.lnotes;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.widget.Toast;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.FeatureDetector;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;

import data.MatKey_Serial;


public class MarkerProcessingService extends IntentService {
    String marker_path,image_path,file_path;
    private static int FOREGROUND_ID=1337;
    public MarkerProcessingService() {
        super("ImageProcessingService");
    }



    @Override
    protected void onHandleIntent(Intent intent) {
        Mat sceneImage;
        OpenCVLoader.initDebug();
        marker_path=(String)intent.getExtras().get("Marker");
        image_path=(String)intent.getExtras().get("Image");
        Dbhandler dbhandler=new Dbhandler(this,null,null,1);
        Notification notification;
        String CHANNEL_TWO_ID = "com.links.gaurav.lnotes.markerprocessing";
        String CHANNEL_TWO_NAME = "Channel Two";
        NotificationChannel notificationChannel = null;
        NotificationManager notificationManager=(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel = new NotificationChannel(CHANNEL_TWO_ID,
                    CHANNEL_TWO_NAME, NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setShowBadge(true);
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            notificationManager.createNotificationChannel(notificationChannel);
            notification = new Notification.Builder(getApplicationContext(), CHANNEL_TWO_ID)
                    .setContentTitle("Processing Marker")
                    .setContentText("Processing..")
                    .setSmallIcon(android.R.drawable.stat_notify_sync).
                    setTicker("Processing M..").build();
        }else {
            notification = new Notification.Builder(this)
                    .setContentTitle("Processing Marker")
                    .setContentText("Processing..")
                    .setSmallIcon(android.R.drawable.stat_notify_sync).
                            setTicker("Processing M..").build();
        }

        startForeground(FOREGROUND_ID,notification);


        MatOfKeyPoint sceneKeyPoints = new MatOfKeyPoint();
        MatOfKeyPoint sceneDescriptors = new MatOfKeyPoint();
        FeatureDetector featureDetector = FeatureDetector.create(FeatureDetector.ORB);
        DescriptorExtractor descriptorExtractor = DescriptorExtractor.create(DescriptorExtractor.ORB);
        sceneImage= Imgcodecs.imread(marker_path,Imgcodecs.IMREAD_GRAYSCALE);
        featureDetector.detect(sceneImage, sceneKeyPoints);
        descriptorExtractor.compute(sceneImage, sceneKeyPoints, sceneDescriptors);
        float[] data=new float[(int)sceneDescriptors.total()*sceneDescriptors.channels()];
        sceneDescriptors.convertTo(sceneDescriptors,CvType.CV_32F);
        sceneDescriptors.get(0,0,data);
        try{
            File file=File.createTempFile((new File(marker_path)).getName().replaceFirst("[.][^.]+$", ""),".dat",getExternalFilesDir("Pictures"));
            FileOutputStream fos = new FileOutputStream(file.getAbsolutePath());
            ByteBuffer buffer = ByteBuffer.allocate(data.length * 4);
            for (int i = 0; i < data.length; i++){
                buffer.putFloat(data[i]);
            }
            byte[] byteArray = buffer.array();
            fos.write(byteArray);
            fos.close();
            file_path=file.getAbsolutePath();
            dbhandler.setMarker(image_path,marker_path,file_path,sceneDescriptors.rows(),sceneDescriptors.cols());
            //dbhandler.set_marker_KeyPoints(marker_path,file_path,sceneDescriptors.rows(),sceneDescriptors.cols());


        }catch (Exception e){

        }
        /*sceneDescriptors.convertTo(sceneDescriptors,CvType.CV_32F);
        MatKey_Serial matKey_serial=new MatKey_Serial(sceneDescriptors.toArray());
        try{

            File file=File.createTempFile((new File(marker_path)).getName().replaceFirst("[.][^.]+$", ""),".dat",getExternalFilesDir("Pictures"));
            FileOutputStream fos = new FileOutputStream(file.getAbsolutePath());
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(matKey_serial);
            oos.close();
            file_path=file.getAbsolutePath();
            dbhandler.set_marker_KeyPoints(marker_path,file_path,sceneDescriptors.rows(),sceneDescriptors.cols());


        }catch (Exception e){

        }*/
        stopForeground(true);

    }
}
