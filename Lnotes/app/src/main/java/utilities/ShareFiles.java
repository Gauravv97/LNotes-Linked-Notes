package utilities;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.print.PrintAttributes;
import android.print.pdf.PrintedPdfDocument;
import android.support.v4.content.FileProvider;

import com.links.gaurav.lnotes.Dbhandler;
import com.links.gaurav.lnotes.MainActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import data.entry;

/**
 * Created by Gaurav on 3/3/2018.
 */

public class ShareFiles {
    public enum ShareMode{images,pdf}
    public static void share(Context context,Integer []id,ShareMode shareMode){
        Dbhandler dbhandler=new Dbhandler(context,null,null,1);
        entry e[]=new entry[id.length];
        for (int i=0;i<id.length;i++) {
            e[i]=dbhandler.getresult(id[i]);
        }
        switch (shareMode){
            case images:ShareImage(context,e);
                return;
            case pdf:SharePdf(context,e);
                return;
        }



    }
    public static void openPDF(Context context,String []path){
        Uri uri;
        PdfDocument document=BitmapFunctions.CreatePdf(path,context);
        File pdf;
        try {
            pdf = File.createTempFile(
                    "Pdf",
                    ".pdf",
                    context.getExternalFilesDir("TEMP")
            );
            document.writeTo(new FileOutputStream(pdf));
            uri=FileProvider.getUriForFile(context, "com.links.gaurav.lnotes.fileprovider", pdf);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/pdf");
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            context.startActivity(intent);
        } catch (IOException ex) {
            // Error occurred while creating the File
        }

        // close the document
        document.close();

    }
    public static void share(Context context,int id,Integer []serial,ShareMode shareMode){
        Dbhandler dbhandler=new Dbhandler(context,null,null,1);
        entry e=dbhandler.getresult(id);
        String path[]=new String[serial.length];
        for (int i=0;i<serial.length;i++){
            path[i]=e.get_sc()[serial[i]];
        }
        switch (shareMode){
            case images:ShareImage(context,path);
                return;
            case pdf:SharePdf(context,path);
                return;
        }


    }
    private static void ShareImage(Context context,entry [] e){
        ArrayList<Uri> uris=new ArrayList<>(e.length);
        for (entry en:e){
            for (String pth:en.get_sc()){
                File file = new File(pth);
                Uri uri = FileProvider.getUriForFile(context, "com.links.gaurav.lnotes.fileprovider", file);
                uris.add(uri);
            }
        }
        if(!uris.isEmpty()){
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND_MULTIPLE);
            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setType("image/jpg");
            context.startActivity(Intent.createChooser(intent, "Share Your Photo"));
        }

    }
    private static void SharePdf(Context context,entry []e){
        ArrayList<Uri> files = new ArrayList<Uri>();

        for(int i=0;i<e.length;i++){
            PdfDocument document = BitmapFunctions.CreatePdf(e[i].get_sc(),context);
            File image;
            try {
                image = File.createTempFile(
                        "Pdf",
                        ".pdf",
                        context.getExternalFilesDir("TEMP")
                );
                document.writeTo(new FileOutputStream(image));
                files.add(FileProvider.getUriForFile(context, "com.links.gaurav.lnotes.fileprovider", image));
            } catch (IOException ex) {
                // Error occurred while creating the File
            }

            // close the document
            document.close();
        }
        if(!files.isEmpty()){
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND_MULTIPLE);
            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files);
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setType("application/pdf");
            context.startActivity(Intent.createChooser(intent, "Share Your Photo"));
        }




        // write the document content



    }
    private static void ShareImage(Context context,String []path){
        ArrayList<Uri> uris=new ArrayList<>(path.length);
            for (String pth:path){
                File file = new File(pth);
                Uri uri = FileProvider.getUriForFile(context, "com.links.gaurav.lnotes.fileprovider", file);
                uris.add(uri);
            }
        if(!uris.isEmpty()){
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND_MULTIPLE);
            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setType("image/jpg");
            context.startActivity(Intent.createChooser(intent, "Share Your Photo"));
        }

    }
    private static void SharePdf(Context context,String []path){
        ArrayList<Uri> files = new ArrayList<Uri>();
        PdfDocument document =BitmapFunctions.CreatePdf(path,context);
        File image;
        try {
            image = File.createTempFile(
                    "PDF",
                    ".pdf",
                    context.getExternalFilesDir("TEMP")
            );
            document.writeTo(new FileOutputStream(image));
            files.add(FileProvider.getUriForFile(context, "com.links.gaurav.lnotes.fileprovider", image));
        } catch (IOException ex) {
            // Error occurred while creating the File
        }

        // close the document
        document.close();
        if(!files.isEmpty()){
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND_MULTIPLE);
            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files);
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setType("application/pdf");
            context.startActivity(Intent.createChooser(intent, "Share Your Photo"));
        }

    }


}
