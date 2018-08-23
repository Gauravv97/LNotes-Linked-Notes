package com.links.gaurav.lnotes;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.SparseArray;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;


import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterInside;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import utilities.BitmapFunctions;

/**
 * Created by Gaurav on 3/2/2018.
 */

public class OCRReader extends AppCompatActivity {
    private TextRecognizer detector;
    RelativeLayout loadingPanel;

    String path;
    EditText OcrText;
    Toolbar toolbar;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ocr_layout);
        loadingPanel=findViewById(R.id.loadingPanel);
        OcrText=findViewById(R.id.OCR_text);
        toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        path=getIntent().getStringExtra("path");
        detector= new TextRecognizer.Builder(getApplicationContext()).build();
        ImageView imageView=findViewById(R.id.OCR_image);
        Glide.with(this).load(path).apply(RequestOptions.bitmapTransform(new CenterInside())).into(imageView);
        if(detector.isOperational()){
            new RecognizeTextTask().execute();
        }else {
            OcrText.setText("OCR is not Operational right now.!\n Try again later");
            Toast.makeText(OCRReader.this,"Try again after some time",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    class RecognizeTextTask extends AsyncTask<Void,Void,Void>{
        SparseArray<TextBlock> textBlocks;
        @Override
        protected void onPreExecute() {
            loadingPanel.setVisibility(View.VISIBLE);
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            loadingPanel.setVisibility(View.GONE);
            String blocks="";
            for (int index = 0; index < textBlocks.size(); index++) {
                //extract scanned text blocks here
                TextBlock tBlock = textBlocks.valueAt(index);
                blocks = blocks + tBlock.getValue() + "\n" + "\n";

            }
            if(!blocks.equals(""))
                OcrText.setText(blocks);
            else Toast.makeText(OCRReader.this,"No Text Found",Toast.LENGTH_SHORT).show();
            super.onPostExecute(aVoid);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Bitmap tmp= BitmapFunctions.decodeSampledBitmapFromResource(path,1500,1500);
            Frame frame=new Frame.Builder().setBitmap(tmp).build();
            textBlocks = detector.detect(frame);
            tmp.recycle();
            return null;
        }
    }
}
