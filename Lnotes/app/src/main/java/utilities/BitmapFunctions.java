package utilities;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Environment;
import android.support.media.ExifInterface;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Gaurav on 2/21/2018.
 */

public class BitmapFunctions {
    private static final float maxHeight = 3264.0f;
    private static final float maxWidth = 3264.0f;
    public static String compressImage(String imagePath,int compression,String newImagePath) {

        Bitmap scaledBitmap = null;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Bitmap bmp = BitmapFactory.decodeFile(imagePath, options);

        int actualHeight = options.outHeight;
        int actualWidth = options.outWidth;

        float imgRatio = (float) actualWidth / (float) actualHeight;
        float maxRatio = maxWidth / maxHeight;

        if (actualHeight > maxHeight || actualWidth > maxWidth) {
            if (imgRatio < maxRatio) {
                imgRatio = maxHeight / actualHeight;
                actualWidth = (int) (imgRatio * actualWidth);
                actualHeight = (int) maxHeight;
            } else if (imgRatio > maxRatio) {
                imgRatio = maxWidth / actualWidth;
                actualHeight = (int) (imgRatio * actualHeight);
                actualWidth = (int) maxWidth;
            } else {
                actualHeight = (int) maxHeight;
                actualWidth = (int) maxWidth;

            }
        }

        options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);
        options.inJustDecodeBounds = false;
        options.inDither = false;
        options.inPurgeable = true;
        options.inInputShareable = true;
        options.inTempStorage = new byte[16 * 1024];

        try {
            bmp = BitmapFactory.decodeFile(imagePath, options);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();

        }
        try {
            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_4444);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();
        }

        float ratioX = actualWidth / (float) options.outWidth;
        float ratioY = actualHeight / (float) options.outHeight;
        float middleX = actualWidth / 2.0f;
        float middleY = actualHeight / 2.0f;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2, middleY - bmp.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));

        if(bmp!=null)
        {
            bmp.recycle();
        }

        ExifInterface exif;
        try {
            exif = new ExifInterface(imagePath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0);
            Matrix matrix = new Matrix();
            if (orientation == 6) {
                matrix.postRotate(90);
            } else if (orientation == 3) {
                matrix.postRotate(180);
            } else if (orientation == 8) {
                matrix.postRotate(270);
            }
            scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        FileOutputStream out = null;
        String filepath = newImagePath;
        try {
            out = new FileOutputStream(filepath);

            //write the compressed bitmap at the destination specified by filename.
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, compression, out);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return filepath;
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        final float totalPixels = width * height;
        final float totalReqPixelsCap = reqWidth * reqHeight * 2;

        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++;
        }

        return inSampleSize;
    }
    public static Bitmap decodeSampledBitmapFromResource(String path, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        File file=new File(path);
        int len;
        byte[] bytes;
        try {
            FileInputStream fis = new FileInputStream(file);
            len=(int) file.length();
            bytes = new byte[len];
            fis.read(bytes);


            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(bytes, 0, len, options);


            // Calculate inSampleSizes
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);


            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
            return  BitmapFactory.decodeByteArray(bytes, 0, len, options);
        }catch (Exception e){
            Log.e("mm","ss",e);}
        return null;
    }
    public static Pair<Integer,Integer> getHeightWidthOfImage(String Path){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(Path,options);
        return new Pair<>(options.outHeight,options.outWidth);
    }
    public static PdfDocument CreatePdf(String []path,Context context){
        /*int Width=Resources.getSystem().getDisplayMetrics().widthPixels*2;
        int Height=Resources.getSystem().getDisplayMetrics().heightPixels*2;
        if(Height<Width){
            Height=Width;
            Width=Resources.getSystem().getDisplayMetrics().heightPixels*2;

        }*/
        int Width=720,Height=1280;



        PdfDocument document = new PdfDocument();
        for(int i=0;i<path.length;i++){


            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(Width,Height, i+1).create();
            PdfDocument.Page page = document.startPage(pageInfo);
            Canvas canvas = page.getCanvas();
            
            Paint paint = new Paint();
            paint.setColor(Color.parseColor("#ffffff"));
            canvas.drawPaint(paint);
            Pair<Integer,Integer> HW=getHeightWidthOfImage(path[i]);
            int bWidth,bHeight;
            if(HW.first*1.0/HW.second<Height*1.0/Width){
                bWidth=Width;
                bHeight=(HW.first*Width)/HW.second;
            }
            else {
                bHeight=Height;
                bWidth=(HW.second*Height)/HW.first;
            }
            Bitmap bitmap1 =BitmapFactory.decodeFile(path[i]);
            //Bitmap bitmap= BitmapFunctions.decodeSampledBitmapFromResource(path[i], bWidth,bHeight);
            Bitmap bitmap=Bitmap.createScaledBitmap(bitmap1,bWidth,bHeight,true);
            bitmap1.recycle();
            /*Bitmap bitmap= BitmapFunctions.decodeSampledBitmapFromResource(path[i], Width,Height);
            bitmap = Bitmap.createScaledBitmap(bitmap, Width, bitmap.getHeight()*Width/bitmap.getWidth(), true);*/
            int pdfTop=0,pdfLeft=0;
            if(HW.first*1.0/HW.second<Height*1.0/Width)
                pdfTop=Height/2-bHeight/2;
            else
                pdfLeft=Width/2-bWidth/2;

            paint.setColor(Color.BLUE);
            canvas.drawBitmap(bitmap, pdfLeft, pdfTop , null);
            //canvas.drawBitmap(bitmap, 0, 0 , null);
            /*imageView.setImageBitmap(bitmap);
            linearLayout.draw(canvas);*/
            bitmap.recycle();
            document.finishPage(page);

        }
        return document;
    }
    private View getView(Context context){

        return new View(context);
    }

}
