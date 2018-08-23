package com.links.gaurav.lnotes;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.widget.Toast;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.features2d.Params;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import data.ImageProperties;
import utilities.BitmapFunctions;
import utilities.ImageProcessor;

public class ImageProcessingService extends IntentService {

    private static final String AUTHORITY=
            BuildConfig.APPLICATION_ID+".provider";
    private static int NOTIFY_ID=2296;
    private static int FOREGROUND_ID=2797;
    private ImageProperties[] imageProperties;
    private int i;
    private int colorThresh = 110;
    public ImageProcessingService() {
        super("ImageProcessingService");

    }



    @Override
    protected void onHandleIntent(Intent intent) {
        imageProperties=(ImageProperties[])intent.getExtras().get("ImageProperties");
        int i,l= imageProperties.length,ID=-1;
        Notification notification;
        Boolean doesFileExists;
        String CHANNEL_ONE_ID = "com.links.gaurav.lnotes.imageprocessing";
        String CHANNEL_ONE_NAME = "Channel One";
        NotificationChannel notificationChannel = null;
        NotificationManager notificationManager=(NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        SharedPreferences mSharedPrefs= PreferenceManager.getDefaultSharedPreferences(this);
        int compression=mSharedPrefs.getInt(SettingsFragment.KEY_PREF_SCANQUALITY,10);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel = new NotificationChannel(CHANNEL_ONE_ID,
                    CHANNEL_ONE_NAME, NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setShowBadge(true);
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            notificationManager.createNotificationChannel(notificationChannel);
            notification = new Notification.Builder(getApplicationContext(), CHANNEL_ONE_ID)
                    .setContentTitle("Processing Images")
                    .setContentText("Processing Image 0 out of "+l)
                    .setSmallIcon(android.R.drawable.stat_notify_sync).
                            setTicker("Processing 0..").build();
        }else{

            notification=new Notification.Builder(this)
                    .setContentTitle("Processing Images")
                    .setContentText("Processing Image 0 out of "+l)
                    .setSmallIcon(android.R.drawable.stat_notify_sync).
                            setTicker("Processing 0..").build();
        }


        startForeground(FOREGROUND_ID,notification);

        Dbhandler dbhandler=new Dbhandler(this,null,null,1);
        Mat doc;

        for(i=0;i<l;i++) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                notification = new Notification.Builder(getApplicationContext(), CHANNEL_ONE_ID)
                        .setContentTitle("Processing Images")
                        .setContentText("Processing Image " + (i + 1) + " out of " + l)
                        .setSmallIcon(android.R.drawable.stat_notify_sync).
                                setTicker("Processing " + (i + 1) + "..").build();
            } else {
                notification = new Notification.Builder(this)
                        .setContentTitle("Processing Images")
                        .setContentText("Processing Image " + (i + 1) + " out of " + l)
                        .setSmallIcon(android.R.drawable.stat_notify_sync).
                                setTicker("Processing " + (i + 1) + "..").build();

            }
            notificationManager.notify(FOREGROUND_ID, notification);
            if(dbhandler.CheckIfFileExists(imageProperties[i].getOriginal_Image()))
            {
                doc = CropfourPoints.fourPointTransform(Imgcodecs.imread(imageProperties[i].getOriginal_Image(), Imgcodecs.IMREAD_UNCHANGED), imageProperties[i].getPoints());
                //1=CW, 2=CCW, 3=180

                try {
                    File temp = createImageFile();
                    ImageProcessor.ProcessImage(doc,imageProperties[i].getScanMode(),imageProperties[i].getContrast(),imageProperties[i].getDetails(),2);
                    int rotflag = ((int) imageProperties[i].getRotation() / 90) % 4;
                    if (rotflag == 1) {
                        Core.rotate(doc, doc, Core.ROTATE_90_CLOCKWISE);
                    } else if (rotflag == 2) {
                        Core.rotate(doc, doc, Core.ROTATE_180);
                    } else if (rotflag == 3) {
                        Core.rotate(doc, doc, Core.ROTATE_90_COUNTERCLOCKWISE);
                    }

                /*Imgproc.cvtColor(doc, doc, Imgproc.COLOR_RGBA2GRAY);
                Imgproc.adaptiveThreshold(doc, doc, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 15, 15);*/
                    Imgcodecs.imwrite(temp.getAbsolutePath(), doc);
                    doc.release();
                    File compressed=createImageFile();
                    BitmapFunctions.compressImage(temp.getAbsolutePath(),compression,compressed.getAbsolutePath());
                    temp.delete();
                    ID = dbhandler.set_scannedImage(imageProperties[i].getOriginal_Image(), compressed.getAbsolutePath());
                    if (ID == -1) {
                        compressed.delete();
                        continue;
                    }
                } catch (IOException e) {
                }
                Intent localIntent = new Intent("ImageProcessingCompleteBrodcast");
                localIntent.putExtra("ID", ID);
                // Broadcasts the Intent to receivers in this app.
                LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
            }
        }
        stopForeground(true);
    }
    private File createImageFile() throws IOException {

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "Scanned_" + timeStamp + "_";

        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                getExternalFilesDir("Pictures")      /* directory */
        );
        return image;
    }

}
